package org.example.sansam.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.product.domain.Category;
import org.example.sansam.product.domain.Product;
import org.example.sansam.product.repository.ProductJpaRepository;
import org.example.sansam.product.service.ProductService;
import org.example.sansam.s3.domain.FileManagement;
import org.example.sansam.s3.service.FileService;
import org.example.sansam.search.dto.PageResponse;
import org.example.sansam.search.dto.SearchItemResponse;
import org.example.sansam.search.dto.SearchListResponse;
import org.example.sansam.search.dto.SearchProductDocument;
import org.example.sansam.user.service.UserService;
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
@Slf4j
public class SearchService {

    private final static String LIKE_KEY = "like:products";
    private final static String VIEWCOUNT_KEY = "viewcount:products";
    private final static String RECOMMEND_KEY = "recommend:products";
    private static final long CACHE_TTL =  120L;

    private final ProductJpaRepository productJpaRepository;
    private final WishJpaRepository wishJpaRepository;
    private final FileService fileService;
    private final UserService userService;
    private final ProductService productService;
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

    private String buildBrandCacheKey(String keyword, int page) {
        return String.format("search:%s:%d", keyword, page);
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
        long start, end;

//        start = System.nanoTime();
//        Page<SearchItemResponse> cached = getFromCache(cacheKey);
//        end = System.nanoTime();
//        System.out.println("⏱ getFromCache: " + (end - start) / 1_000_000 + " ms");
//
//        if (cached != null) {
//            return cached;
//        }

        start = System.nanoTime();
        SearchResponse<SearchProductDocument> response = executeSearch(boolQuery, sort, page, size);
        end = System.nanoTime();
        System.out.println("⏱ executeSearch: " + (end - start) / 1_000_000 + " ms");

        start = System.nanoTime();
        List<SearchProductDocument> documents = extractDocuments(response);
        end = System.nanoTime();
        System.out.println("⏱ extractDocuments: " + (end - start) / 1_000_000 + " ms");

        start = System.nanoTime();
        Set<Long> wishedProductIds = getWishedProductIds(userId, documents);
        end = System.nanoTime();
        System.out.println("⏱ getWishedProductIds: " + (end - start) / 1_000_000 + " ms");

        start = System.nanoTime();
        List<SearchItemResponse> responses = toDtoList(documents, wishedProductIds);
        end = System.nanoTime();
        System.out.println("⏱ toDtoList: " + (end - start) / 1_000_000 + " ms");

        start = System.nanoTime();
        PageImpl<SearchItemResponse> pageResult = buildPage(responses, page, size, response);
        end = System.nanoTime();
        System.out.println("⏱ buildPage: " + (end - start) / 1_000_000 + " ms");

//        start = System.nanoTime();
//        saveToCache(cacheKey, pageResult);
//        end = System.nanoTime();
//        System.out.println("⏱ saveToCache: " + (end - start) / 1_000_000 + " ms");

        Set<String> keys = redisTemplate.keys("*");
        if (keys != null) {
            keys.forEach(System.out::println);
        }

        return pageResult;
    }


    private List<SearchItemResponse> getPageResultBrand(
            BoolQuery boolQuery, String cacheKey,
            Long userId, int size, int offset, String sort
    ) throws IOException {
        List<SearchItemResponse> cached = getFromBrandCache(cacheKey);
        if (cached != null) {
            System.out.println("from cache-----");
            return cached;
        }
        System.out.println("XXX cache-----");

        SearchResponse<SearchProductDocument> response =
                executeSearchBrand(boolQuery, sort, size, offset);

        List<SearchProductDocument> documents = extractDocuments(response);
        Set<Long> wishedProductIds = getWishedProductIds(userId, documents);
        List<SearchItemResponse> responses = toDtoList(documents, wishedProductIds);

        saveToBrandCache(cacheKey, responses);

        Set<String> keys = redisTemplate.keys("*");
        if (keys != null) {
            keys.forEach(System.out::println);
        }

        return responses;
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

    private List<SearchItemResponse> getFromBrandCache(String cacheKey) {
        Object raw = redisTemplate.opsForValue().get(cacheKey);
        if (raw != null) {
            List<SearchItemResponse> cached =
                    objectMapper.convertValue(raw, new TypeReference<>() {});
            return cached;
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

    private void saveToBrandCache(String cacheKey, List<SearchItemResponse> result) {
        List<SearchItemResponse> cacheData = result;

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

    private SearchResponse<SearchProductDocument> executeSearchBrand(
            BoolQuery boolQuery, String sort, int size, int offset
    ) throws IOException {
        SearchRequest request = new SearchRequest.Builder()
                .index("products")
                .query(q -> q.bool(boolQuery))
                .from(offset)
                .size(size)
                .build();

        log.info("search request : {}", request);
        return esClient.search(request, SearchProductDocument.class);
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

                    boolean isWished = wishedProductIds != null ? wishedProductIds.contains(product.getProductId()) : false;
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

    public Sort getSort1(String sortKey) {
        switch (sortKey) {
            case "price":
                return Sort.by(Sort.Direction.ASC, "price");
            case "viewCount":
                return Sort.by(Sort.Direction.DESC, "view_count");
            case "createdAt":
            default:
                return Sort.by(Sort.Direction.DESC, "created_at");
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
                .size(40)
                .sort(s -> s.field(f -> f.field("wishCount").order(SortOrder.Desc)))
                .build();
    }

    private SearchRequest buildTopViewRequest() {
        return new SearchRequest.Builder()
                .index("products")
                .size(40)
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

    @Transactional
    public Page<SearchItemResponse> searchKeywordProductListV1(
            String keyword, Long userId,
            int page, int size, String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, getSort(sort));

        long start = System.currentTimeMillis();
        Page<Product> products = productJpaRepository.findByKeywordV1(keyword, pageable);
        long end = System.currentTimeMillis();
        log.info("⏱ productJpaRepository.findByKeywordV1 실행 시간 = {} ms", (end - start));

        Set<Long> wishedProductIds = new HashSet<>();

        if (userId != null) {
            List<Long> productIds = products.getContent().stream()
                    .map(Product::getId)
                    .collect(Collectors.toList());

            start = System.currentTimeMillis();
            wishedProductIds = wishJpaRepository.findByUserIdAndProductIdIn(userId, productIds)
                    .stream()
                    .map(wish -> wish.getId())
                    .collect(Collectors.toSet());
            end = System.currentTimeMillis();
            log.info("⏱ wishJpaRepository.findByUserIdAndProductIdIn 실행 시간 = {} ms", (end - start));
        }

        final Set<Long> finalWishedProductIds = wishedProductIds;

        return products.map(product -> {
            boolean isWished = finalWishedProductIds.contains(product.getId());

            long startTime = System.currentTimeMillis();
            String imageUrl = Optional.ofNullable(product.getFileManagement())
                    .map(file -> fileService.getImageUrl(file.getId()))
                    .orElse(null);
            long endTime = System.currentTimeMillis();
            log.info("⏱ fileService.getImageUrl 실행 시간 = {} ms", (endTime - startTime));

            Category category = product.getCategory();
            return SearchItemResponse.builder()
                    .productId(product.getId())
                    .productName(product.getProductName())
                    .price(product.getPrice())
                    .url(imageUrl)
                    .wish(isWished)
                    .category(Category.toCategoryString(
                            category.getBigName(),
                            category.getMiddleName(),
                            category.getSmallName()))
                    .build();
        });
    }

    @Transactional
    public Page<SearchItemResponse> searchCategoryProductListV1(
            String big, String middle, String small, Long userId,
            int page, int size, String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, getSort(sort));

        Page<Product> products = productJpaRepository.findByCategoryNamesV1(big, middle, small, pageable);
        Set<Long> wishedProductIds = new HashSet<>();
        if (userId != null) {
            List<Long> productsIds = products.getContent().stream()
                    .map(Product::getId)
                    .collect(Collectors.toList());
            wishedProductIds = wishJpaRepository.findByUserIdAndProductIdIn(userId, productsIds)
                    .stream()
                    .map(wish -> wish.getId())
                    .collect(Collectors.toSet());
        }
        final Set<Long> finalWishedProductIds = wishedProductIds;
        return products.map(product -> {
            boolean isWished = finalWishedProductIds.contains(product.getId());

            String imageUrl = Optional.ofNullable(product.getFileManagement())
                    .map(file -> fileService.getImageUrl(file.getId()))
                    .orElse(null);

            Category category = product.getCategory();
            return SearchItemResponse.builder()
                    .productId(product.getId())
                    .productName(product.getProductName())
                    .price(product.getPrice())
                    .url(imageUrl)
                    .wish(isWished)
                    .category(Category.toCategoryString(category.getBigName(), category.getMiddleName(), category.getSmallName()))
                    .build();
        });
    }
//
//    private List<SearchListResponse> productToDto0(List<Product> products,Long userId) {
//        return products.stream()
//                .map(product -> {
//                    boolean wished = (userId != null) &&
//                            wishJpaRepository.findByUserIdAndProductId(userId, product.getId()).isPresent();
//                    String imageUrl = (product.getFileManagement() != null)
//                            ? fileService.getImageUrl(product.getFileManagement().getId())
//                            : null;
//
//                    return SearchListResponse.builder()
//                            .productId(product.getId())
//                            .productName(product.getProductName())
//                            .price(product.getPrice()) // DTO가 int면 변환
//                            .url(imageUrl)
//                            .wish(wished)
//                            .build();
//                })
//                .collect(Collectors.toList());
//    }

    public List<SearchListResponse> getProductsByLikeV1(Long userId) {
        List<Product> products = productJpaRepository.findTopWishListProduct();
        return productToDto(products, userId);
    }

    //상품 추천 - 위시에 상품이 있는 경우 -> 위시에 있는 상품과 같은 카테고리에 있는 상품 랜덤 추천 / 상품 위시에 없거나 유저 로그인X 시 -> 상품 조회순으로 표시
    public List<SearchListResponse> getProductsByRecommendV1(Long userId) {
        Wish wish = wishJpaRepository.findTopByUserIdOrderByCreated_atDesc(userId);
        List<Product> products;
        if(wish == null) {
            products = productJpaRepository.findTopWishListProduct();
            System.out.println("기본 조회수순");
        } else {
            Product product = productJpaRepository.findById(wish.getId())
                    .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));
            products = productJpaRepository.findByCategoryOrderByViewCountDesc(product.getCategory());
            System.out.println("추천순");
        }
        return productToDto(products,userId);
    }

    // -------------V2-------------------------

    @Transactional
    public Page<SearchItemResponse> searchKeywordProductListV2_1(
            String keyword, Long userId,
            int page, int size, String sort
    ) {
        List<Long> categories = productService.getCategorysId(keyword);

        List<Long> keywordIds = productJpaRepository.findIdsByKeyword(keyword);
        List<Long> categoryIds = (categories != null && !categories.isEmpty())
                ? productJpaRepository.findIdsByCategories(categories)
                : Collections.emptyList();
        log.info(keywordIds.size() + " " + categoryIds.size());

        Set<Long> intersectionIds;
        if (!categoryIds.isEmpty()) {
            intersectionIds = new HashSet<>(keywordIds);
            intersectionIds.retainAll(categoryIds);
        } else if (keywordIds.isEmpty()) {
            intersectionIds = new HashSet<>(categoryIds);
        } else {
            intersectionIds = new HashSet<>(keywordIds);
        }

        List<Long> idList = new ArrayList<>(intersectionIds);
        int start = (int) PageRequest.of(page, size).getOffset();
        int end = Math.min(start + size, idList.size());
        List<Long> pagedIds = (start <= end) ? idList.subList(start, end) : Collections.emptyList();

        List<Product> products = pagedIds.isEmpty()
                ? Collections.emptyList()
                : productJpaRepository.findAllByIds(pagedIds, PageRequest.of(page, size));

        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(page, size), intersectionIds.size());

        Set<Long> wishedProductIds = new HashSet<>();
        if (userId != null && userService.findById(userId).isPresent() && !pagedIds.isEmpty()) {
            wishedProductIds = wishJpaRepository.findByUserIdAndProductIdIn(userId, pagedIds)
                    .stream()
                    .map(Wish::getId)
                    .collect(Collectors.toSet());
        }
        final Set<Long> finalWishedProductIds = wishedProductIds;

        Map<Long, String> urls = products.stream()
                .map(Product::getFileManagement)
                .collect(Collectors.toMap(FileManagement::getId, FileManagement::getFileUrl));

        return productPage.map(product -> {
            boolean isWished = finalWishedProductIds.contains(product.getId());
            String imageUrl = urls.get(product.getFileManagement().getId());
            Category category = product.getCategory();

            return SearchItemResponse.builder()
                    .productId(product.getId())
                    .productName(product.getProductName())
                    .price(product.getPrice())
                    .url(imageUrl)
                    .wish(isWished)
                    .category(Category.toCategoryString(
                            category.getBigName(),
                            category.getMiddleName(),
                            category.getSmallName()))
                    .build();
        });
    }

    @Transactional(readOnly = true)
    public Page<SearchItemResponse> searchKeywordProductListV2(
            String keyword, Long userId,
            int page, int size, String sort
    ) {
        List<Long> categories = productService.getCategorysId(keyword);
        log.info(categories.size() + " " + keyword);

        String booleanKeyword = Arrays.stream(keyword.trim().split("\\s+"))
                .map(word -> "+" + word + "*")
                .collect(Collectors.joining(" "));

        List<Long> candidateIds;
        if (categories == null || categories.isEmpty()) {
            candidateIds = productJpaRepository.findCandidateIdsWithoutCategory(booleanKeyword, PageRequest.of(page, size).getPageSize());
        } else {
            candidateIds = productJpaRepository.findCandidateIdsWithCategory(booleanKeyword, categories, PageRequest.of(page, size).getPageSize());
        }


        if (candidateIds.isEmpty()) {
            return Page.empty(PageRequest.of(page, size));
        }

        List<Object[]> ranked = productJpaRepository.rankCandidatesWithCategory(
                keyword, candidateIds, categories, size
        );

        List<Long> pagedIds = ranked.stream()
                .map(row -> (Long) row[0])  // product_id
                .toList();

        if (pagedIds.isEmpty()) {
            return Page.empty(PageRequest.of(page, size));
        }

        List<Product> products = productJpaRepository.findAllByIds(pagedIds, PageRequest.of(page, size));

        Set<Long> wishedProductIds = new HashSet<>();
        if (userId != null && userService.findById(userId).isPresent()) {
            wishedProductIds = wishJpaRepository.findByUserIdAndProductIdIn(userId, pagedIds)
                    .stream()
                    .map(Wish -> Wish.getProduct().getId())
                    .collect(Collectors.toSet());
        }
        final Set<Long> finalWishedProductIds = wishedProductIds;

        Map<Long, String> urls = products.stream()
                .collect(Collectors.toMap(
                        p -> p.getFileManagement().getId(),
                        p -> p.getFileManagement().getFileUrl()
                ));

        List<SearchItemResponse> responses = products.stream().map(product -> {
            boolean isWished = finalWishedProductIds.contains(product.getId());
            String imageUrl = urls.get(product.getFileManagement().getId());
            Category category = product.getCategory();

            return SearchItemResponse.builder()
                    .productId(product.getId())
                    .productName(product.getProductName())
                    .price(product.getPrice())
                    .url(imageUrl)
                    .wish(isWished)
                    .category(Category.toCategoryString(
                            category.getBigName(),
                            category.getMiddleName(),
                            category.getSmallName()))
                    .build();
        }).toList();

        return new PageImpl<>(responses, PageRequest.of(page, size), candidateIds.size());
    }

//    @Transactional
//    public Page<SearchItemResponse> searchKeywordProductListV2(
//            String keyword, Long userId,
//            int page, int size, String sort
//    ) {
//        List<Long> categories = productService.getCategorysId(keyword);
//
//
//        List<Product> productsByKeyword = productJpaRepository.findByKeywordV2(keyword);
//        List<Product> productsByCategory = new ArrayList<>();
//        PageImpl<Product> products;
//        if(categories != null) {
//            log.info("------- categories not null ----------");
//            log.info("------- categories size = {} ----------", categories.size() + " category " + categories.toString());
//            productsByKeyword = productJpaRepository.findByCategoryListV2(categories);
//            List<Product> intersection = new ArrayList<>(productsByKeyword);
//            intersection.retainAll(productsByCategory);
//            products = new PageImpl<>(intersection, PageRequest.of(page, size), intersection.size());
//        } else {
//            products = new PageImpl<>(productsByKeyword, PageRequest.of(page, size), productsByKeyword.size());
//        }
//
//        Set<Long> wishedProductIds = new HashSet<>();
//        List<Long> productIds = products.getContent().stream()
//                .map(Product::getId)
//                .collect(Collectors.toList());
//        if (userId != null && userService.findById(userId).isPresent()) {
//            log.info("------- user  존재 ----------");
//
//            long start = System.currentTimeMillis();
//            wishedProductIds = wishJpaRepository.findByUserIdAndProductIdIn(userId, productIds)
//                    .stream()
//                    .map(Wish::getId)
//                    .collect(Collectors.toSet());
//            long end = System.currentTimeMillis();
//            log.info("⏱ wishJpaRepository.findByUserIdAndProductIdIn 실행 시간 = {} ms", (end - start));
//        }
//
//        final Set<Long> finalWishedProductIds = wishedProductIds;
//
//        List<FileManagement> fileManagements = products.getContent().stream()
//                .map(Product::getFileManagement)
//                .toList();
//        Map<Long, String> urls = fileManagements.stream()
//                .collect(Collectors.toMap(FileManagement::getId, FileManagement::getFileUrl));
//
//        return products.map(product -> {
//            boolean isWished = finalWishedProductIds.contains(product.getId());
//
//            String imageUrl = urls.get(product.getFileManagement().getId());
//
//            Category category = product.getCategory();
//            return SearchItemResponse.builder()
//                    .productId(product.getId())
//                    .productName(product.getProductName())
//                    .price(product.getPrice())
//                    .url(imageUrl)
//                    .wish(isWished)
//                    .category(Category.toCategoryString(
//                            category.getBigName(),
//                            category.getMiddleName(),
//                            category.getSmallName()))
//                    .build();
//        });
//    }

    @Transactional
    public Page<SearchItemResponse> searchCategoryProductListV2(
            String big, String middle, String small, Long userId,
            int page, int size, String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, getSort(sort));
        Long categoryId = productService.getCategoryId(big, middle, small);
        Page<Product> products = productJpaRepository.findByCategoryNamesV2(categoryId, pageable);
        Set<Long> wishedProductIds = new HashSet<>();
        if (userId != null) {
            List<Long> productsIds = products.getContent().stream()
                    .map(Product::getId)
                    .collect(Collectors.toList());
            wishedProductIds = wishJpaRepository.findByUserIdAndProductIdIn(userId, productsIds)
                    .stream()
                    .map(wish -> wish.getId())
                    .collect(Collectors.toSet());
        }
        final Set<Long> finalWishedProductIds = wishedProductIds;
        return products.map(product -> {
            boolean isWished = finalWishedProductIds.contains(product.getId());

            String imageUrl = Optional.ofNullable(product.getFileManagement())
                    .map(file -> fileService.getImageUrl(file.getId()))
                    .orElse(null);

            Category category = product.getCategory();
            return SearchItemResponse.builder()
                    .productId(product.getId())
                    .productName(product.getProductName())
                    .price(product.getPrice())
                    .url(imageUrl)
                    .wish(isWished)
                    .category(Category.toCategoryString(category.getBigName(), category.getMiddleName(), category.getSmallName()))
                    .build();
        });
    }

    public List<SearchListResponse> getProductsByLikeV2(Long userId) {
        List<Product> products = productJpaRepository.findTopWishListProduct();
        return productToDto(products, userId);
    }

    //상품 추천 - 위시에 상품이 있는 경우 -> 위시에 있는 상품과 같은 카테고리에 있는 상품 랜덤 추천 / 상품 위시에 없거나 유저 로그인X 시 -> 상품 조회순으로 표시
    public List<SearchListResponse> getProductsByRecommendV2(Long userId) {
        Wish wish = wishJpaRepository.findTopByUserIdOrderByCreated_atDesc(userId);
        List<Product> products;
        if(wish == null) {
            products = productJpaRepository.findTopWishListProduct();
            System.out.println("기본 조회수순");
        } else {
            Product product = productJpaRepository.findById(wish.getId())
                    .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));
            products = productJpaRepository.findByCategoryOrderByViewCountDesc(product.getCategory());
            System.out.println("추천순");
        }
        return productToDto(products,userId);
    }

    @Transactional
    public List<SearchItemResponse> searchProductByBrand(
            String brand, Long userId,
            int page
    ) throws IOException {
        int size = (page == 0) ? 11 : 8;
        int offset = (page == 0) ? 0 : (page - 1) * 8 + 11;

        String cacheKey = buildBrandCacheKey(brand, page);
        log.info("page={}, offset={}, size={}", page, offset, size);

        BoolQuery.Builder bool = new BoolQuery.Builder();
        if (brand != null && !brand.isEmpty()) {
            bool.must(m -> m.multiMatch(mm -> mm
                    .query(brand)
                    .fields("brand")
            ));
        }

        return getPageResultBrand(bool.build(), cacheKey, userId, size, offset, "viewCount");
    }
}
