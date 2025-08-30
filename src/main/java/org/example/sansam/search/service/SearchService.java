package org.example.sansam.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.sansam.product.domain.Category;
import org.example.sansam.product.domain.Product;
import org.example.sansam.product.repository.ProductJpaRepository;
import org.example.sansam.s3.service.FileService;
import org.example.sansam.search.dto.PageResponse;
import org.example.sansam.search.dto.SearchItemResponse;
import org.example.sansam.search.dto.SearchListResponse;
import org.example.sansam.search.dto.SearchProductDocument;
import org.example.sansam.wish.domain.Wish;
import org.example.sansam.wish.repository.WishJpaRepository;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final static String LIKE_KEY = "like:products";
    private final static String VIEWCOUNT_KEY = "viewcount:products";
    private final static String RECOMMEND_KEY = "recommend:products";
    private static final long CACHE_TTL =  120L;

    private final ProductJpaRepository productJpaRepository;
    private final WishJpaRepository wishJpaRepository;
    private final FileService fileService;
    private final ElasticsearchClient esClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void evictCache(String keyword, int page, int size, String sort) {
        String cacheKey = buildCacheKey(keyword, page, size, sort);
        redisTemplate.delete(cacheKey);
    }

    private String buildCacheKey(String keyword, int page, int size, String sort) {
        return String.format("search:%s:%d:%d:%s", keyword, page, size, sort);
    }

    private String buildCategoryCacheKey(String category, int page, int size, String sort) {
        return String.format("search:category:%s:%d:%d:%s", category, page, size, sort);
    }

    //키워드 조회, Bool query생성
    @Transactional(readOnly = true)
    public Page<SearchItemResponse> searchProductListByKeyword(
            String keyword, Long userId, int page, int size, String sort
    ) throws IOException {
        String cacheKey = buildCacheKey(keyword, page, size, sort);

        BoolQuery.Builder bool = new BoolQuery.Builder();
        if (keyword != null && !keyword.isEmpty()) {
            bool.must(m -> m.multiMatch(mm -> mm
                    .query(keyword)
                    .fields("productName", "brand", "bigCategory.text", "middleCategory.text", "smallCategory.text")
            ));
        }

        return getPageResult(bool.build(), cacheKey, userId, page, size, sort);
    }

    //카테고리 조회, Bool query생성
    @Transactional(readOnly = true)
    public Page<SearchItemResponse> searchProductListByCategory(
            String big, String middle, String small, Long userId, int page, int size, String sort
    ) throws IOException {
        String category = Category.toCategoryString(big, middle, small);
        String cacheKey = buildCategoryCacheKey(category, page, size, sort);

        BoolQuery.Builder bool = new BoolQuery.Builder();
        if (big != null) bool.filter(f -> f.term(t -> t.field("bigCategory").value(big)));
        if (middle != null) bool.filter(f -> f.term(t -> t.field("middleCategory").value(middle)));
        if (small != null) bool.filter(f -> f.term(t -> t.field("smallCategory").value(small)));

        return getPageResult(bool.build(), cacheKey, userId, page, size, sort);
    }

    //캐시됐는지 확인하고 안되어 있으면 ES에서 조회
    private Page<SearchItemResponse> getPageResult(
            BoolQuery boolQuery, String cacheKey,
            Long userId, int page, int size, String sort
    ) throws IOException {
        Page<SearchItemResponse> cached = getFromCache(cacheKey);
        if (cached != null) {
            System.out.println("from cache-----");
            return cached;
        }
        System.out.println("XXX cache-----");

        SearchResponse<SearchProductDocument> response =
                executeSearch(boolQuery, sort, page, size);

        List<SearchProductDocument> documents = extractDocuments(response);
        Set<Long> wishedProductIds = getWishedProductIds(userId, documents);
        List<SearchItemResponse> responses = toDtoList(documents, wishedProductIds);

        PageImpl<SearchItemResponse> pageResult = buildPage(responses, page, size, response);

        saveToCache(cacheKey, pageResult);

        Set<String> keys = redisTemplate.keys("*");
        if (keys != null) {
            keys.forEach(System.out::println);
        }

        return pageResult;
    }

    //캐시에 저장된 게 있나 확인
    private Page<SearchItemResponse> getFromCache(String cacheKey) {
        Object raw = redisTemplate.opsForValue().get(cacheKey);
        if (raw != null) {
            PageResponse<SearchItemResponse> cached =
                    objectMapper.convertValue(raw, new TypeReference<>() {});
            return new PageImpl<>(
                    cached.getContent(),
                    PageRequest.of(cached.getPage(), cached.getSize()),
                    cached.getTotalElements()
            );
        }
        return null;
    }

    //조회 후 캐시에 저장
    private void saveToCache(String cacheKey, PageImpl<SearchItemResponse> result) {
        PageResponse<SearchItemResponse> cacheData = PageResponse.<SearchItemResponse>builder()
                .content(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();

        redisTemplate.opsForValue().set(cacheKey, cacheData, CACHE_TTL, TimeUnit.SECONDS);
    }

    //ES 조회 및 결과 반환 + 정렬
    private SearchResponse<SearchProductDocument> executeSearch(
            BoolQuery boolQuery, String sort, int page, int size
    ) throws IOException {
        String sortField = (sort == null) ? "createdAt" : sort;
        Set<String> sortableFields = Set.of("price", "viewCount", "createdAt");

        SearchRequest.Builder request = new SearchRequest.Builder()
                .index("products")
                .from(page * size)
                .size(size)
                .trackTotalHits(t -> t.enabled(true))
                .query(q -> q.bool(boolQuery));

        if (sortableFields.contains(sortField)) {
            if ("price".equals(sortField)) {
                request.sort(s -> s.field(f -> f.field(sortField).order(SortOrder.Asc)));
            } else {
                request.sort(s -> s.field(f -> f.field(sortField).order(SortOrder.Desc)));
            }
        }

        return esClient.search(request.build(), SearchProductDocument.class);
    }

    //Elasticsearch 검색 결과에서 실제 _source 문서를 꺼내는 헬퍼 메소드
    private List<SearchProductDocument> extractDocuments(SearchResponse<SearchProductDocument> response) {
        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();
    }

    //위시에 담긴 상품 찾기
    private Set<Long> getWishedProductIds(Long userId, List<SearchProductDocument> documents) {
        if (userId == null || documents.isEmpty()) return Set.of();

        List<Long> productIds = documents.stream()
                .map(SearchProductDocument::getProductId)
                .toList();

        return wishJpaRepository.findByUserIdAndProductIdIn(userId, productIds)
                .stream()
                .map(wish -> wish.getProduct().getId())
                .collect(Collectors.toSet());
    }

    //엔티티를 DTO로 바꿈
    private List<SearchItemResponse> toDtoList(List<SearchProductDocument> documents, Set<Long> wishedProductIds) {
        return documents.stream()
                .map(product -> {
                    boolean isWished = wishedProductIds.contains(product.getProductId());
                    return SearchItemResponse.builder()
                            .productId(product.getProductId())
                            .productName(product.getProductName())
                            .price(product.getPrice())
                            .url(product.getUrl())
                            .wish(isWished)
                            .category(Category.toCategoryString(
                                    product.getBigCategory(),
                                    product.getMiddleCategory(),
                                    product.getSmallCategory()))
                            .build();
                })
                .toList();
    }

    //Page로 반환
    private PageImpl<SearchItemResponse> buildPage(
            List<SearchItemResponse> responses, int page, int size,
            SearchResponse<SearchProductDocument> response
    ) {
        long totalHits = response.hits().total() != null
                ? response.hits().total().value()
                : responses.size();
        return new PageImpl<>(responses, PageRequest.of(page, size), totalHits);
    }

    public Sort getSort(String sortKey) {
        switch (sortKey) {
            case "price":
                return Sort.by(Sort.Direction.ASC, "price");
            case "viewCount":
                return Sort.by(Sort.Direction.DESC, "viewCount");
            case "createdAt":
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }

    public List<SearchListResponse> productToDto(List<Product> products,Long userId) {
        return products.stream()
                .map(product -> {
                    boolean wished = (userId != null) &&
                            wishJpaRepository.findByUserIdAndProductId(userId, product.getId()).isPresent();
                    String imageUrl = (product.getFileManagement() != null)
                            ? fileService.getImageUrl(product.getFileManagement().getId())
                            : null;

                    return SearchListResponse.builder()
                            .productId(product.getId())
                            .productName(product.getProductName())
                            .price(product.getPrice()) // DTO가 int면 변환
                            .url(imageUrl)
                            .wish(wished)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<SearchListResponse> getFromCacheList(String key) {
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            System.out.println("from cache-----");
            return objectMapper.convertValue(cached, new TypeReference<List<SearchListResponse>>() {});
        }
        return null;
    }

    private void saveToCache(String key, List<SearchListResponse> data, long ttlSeconds) {
        redisTemplate.opsForValue().set(key, data, ttlSeconds, TimeUnit.SECONDS);
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null) {
            keys.forEach(System.out::println);
        }

    }

    private List<SearchProductDocument> executeSearch(SearchRequest request) {
        try {
            SearchResponse<SearchProductDocument> response =
                    esClient.search(request, SearchProductDocument.class);
            return extractDocuments(response);
        } catch (IOException e) {
            throw new RuntimeException("ES 검색 실패", e);
        }
    }

    private SearchRequest buildTopWishRequest() {
        return new SearchRequest.Builder()
                .index("products")
                .size(10)
                .sort(s -> s.field(f -> f.field("wishCount").order(SortOrder.Desc)))
                .build();
    }

    private SearchRequest buildTopViewRequest() {
        return new SearchRequest.Builder()
                .index("products")
                .size(10)
                .sort(s -> s.field(f -> f.field("viewCount").order(SortOrder.Desc)))
                .build();
    }

    private SearchRequest buildCategoryRecommendRequest(Category category) {
        BoolQuery.Builder bool = new BoolQuery.Builder();
        if (category.getBigName() != null) {
            bool.filter(f -> f.term(t -> t.field("bigCategory").value(category.getBigName())));
        }
        if (category.getMiddleName() != null) {
            bool.filter(f -> f.term(t -> t.field("middleCategory").value(category.getMiddleName())));
        }
        if (category.getSmallName() != null) {
            bool.filter(f -> f.term(t -> t.field("smallCategory").value(category.getSmallName())));
        }

        return new SearchRequest.Builder()
                .index("products")
                .size(50)
                .trackTotalHits(t -> t.enabled(true))
                .query(q -> q.bool(bool.build()))
                .sort(s -> s.field(f -> f.field("viewCount").order(SortOrder.Desc)))
                .build();
    }

    private List<SearchListResponse> pickRandom(List<SearchListResponse> list, int size) {
        List<SearchListResponse> copy = new ArrayList<>(list);
        Collections.shuffle(copy);
        if (copy.size() > size) {
            return copy.subList(0, size);
        }
        return copy;
    }

    private List<SearchListResponse> toSearchDtoList(List<SearchProductDocument> documents) {
        return documents.stream()
                .map(product -> SearchListResponse.builder()
                        .productId(product.getProductId())
                        .productName(product.getProductName())
                        .price(product.getPrice())
                        .url(product.getUrl())
                        .wish(false)
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SearchListResponse> getProductsByLike(Long userId) {

        List<SearchListResponse> cached = getFromCacheList(LIKE_KEY);
        if (cached != null) return cached;

        List<SearchProductDocument> documents = executeSearch(buildTopWishRequest());
        List<SearchListResponse> responses = toSearchDtoList(documents);

        saveToCache(LIKE_KEY, responses, 300);
        return responses;
    }

    @Transactional(readOnly = true)
    public List<SearchListResponse> getProductsByRecommend(Long userId) {
        Wish wish = wishJpaRepository.findTopByUserIdOrderByCreated_atDesc(userId);

        if (wish == null) {
            List<SearchListResponse> cached = getFromCacheList(VIEWCOUNT_KEY);
            if (cached != null) return cached;

            List<SearchProductDocument> documents = executeSearch(buildTopViewRequest());
            List<SearchListResponse> responses = toSearchDtoList(documents);

            saveToCache(VIEWCOUNT_KEY, responses, 300);
            return responses;
        } else {

            Category category = wish.getProduct().getCategory();
            String cacheKey = String.format("recommend:%d", category.getId());
            List<SearchListResponse> cached = getFromCacheList(cacheKey);
            if (cached != null) return cached;

            List<SearchProductDocument> documents = executeSearch(buildCategoryRecommendRequest(category));
            List<SearchListResponse> responses = toSearchDtoList(documents);

            List<SearchListResponse> random10 = pickRandom(responses, 10);
            saveToCache(cacheKey, random10, 300);
            return random10;
        }
    }

}
