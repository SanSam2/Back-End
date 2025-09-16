package org.example.sansam.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchSuggestionService {

    private final ElasticsearchClient esClient;

    public List<String> getSuggestions(String query) throws IOException {
        if (query == null || query.length() < 2) {
            return List.of();
        }

        SearchResponse<Void> response = esClient.search(s -> s
                        .index("products")
                        .size(0)
                        .query(q -> q.multiMatch(m -> m
                                .query(query)
                                .fields("brand.auto", "productName.auto")
                                .type(TextQueryType.BoolPrefix)
                        ))
                        .aggregations("brand_candidates", a -> a
                                .terms(t -> t
                                        .field("brand.kw")
                                        .size(10)
                                        //.order(o -> o.count(false))
                                )
                        )
                        .aggregations("keyword_candidates", a -> a
                                .terms(t -> t
                                        .field("productName.kw")
                                        .size(30)
                                        //.order(o -> o.count(false))
                                )
                        ),
                Void.class
        );

        // 브랜드 후보
        List<String> brands = response.aggregations()
                .get("brand_candidates")
                .sterms()
                .buckets()
                .array()
                .stream()
                .map(b -> b.key().stringValue())
                .filter(b -> b.contains(query))
                //.filter(this::isKoreanOnly)
                .map(b -> truncate(b, 10))
                .toList();

        // 키워드 후보
        List<String> keywords = response.aggregations()
                .get("keyword_candidates")
                .sterms()
                .buckets()
                .array()
                .stream()
                .map(b -> b.key().stringValue())
                .filter(kw -> kw.contains(query))
                //.filter(this::isKoreanOnly)
                .map(kw -> truncate(kw, 10))
                .toList();

        List<String> suggestions = new ArrayList<>();

        if (!brands.isEmpty()) {
            suggestions.addAll(brands);
            for (String brand : brands) {
                keywords.stream()
                        .limit(3)
                        .forEach(kw -> suggestions.add(truncate(brand + " " + kw, 10)));
            }
        } else {
            // 2. 브랜드 없으면 hits 기반
            SearchResponse<Map> keywordResponse = esClient.search(s -> s
                            .index("products")
                            .size(20)
                            .query(q -> q.match(m -> m
                                    .field("productName.auto")
                                    .query(query)
                            )),
                    Map.class
            );

            List<String> autoKeywords = keywordResponse.hits().hits().stream()
                    .map(hit -> (String) ((Map<?, ?>) hit.source()).get("productName"))
                    .filter(Objects::nonNull)
                    .filter(name -> name.contains(query))
                    //.filter(this::isKoreanOnly)
                    .map(name -> truncate(name, 10))
                    .toList();

            suggestions.addAll(autoKeywords);
        }

        List<String> result = suggestions.stream()
                .distinct()
                .limit(7)
                .toList();

        return result;
    }

    private String truncate(String input, int maxLen) {
        return input.length() <= maxLen ? input : input.substring(0, maxLen);
    }

    private boolean isKoreanOnly(String text) {
        return text.matches("^[가-힣\\s]+$");
    }

}
