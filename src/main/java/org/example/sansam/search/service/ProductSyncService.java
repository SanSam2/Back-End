package org.example.sansam.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.search.dto.ProductDoc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductSyncService {
    private final JdbcTemplate jdbcTemplate;
    private final ElasticsearchClient esClient;

    public void bulkSyncAllProducts() throws IOException {
        String sql = """
        SELECT p.product_id,
               p.product_name,
               p.price,
               p.brand_name,
               c.big_name   AS big,
               c.middle_name AS middle,
               c.small_name AS small,
               fd.url,
               p.created_at,
               p.view_count,
               COUNT(w.wish_id) AS wish_count
        FROM sansam.products p
        JOIN sansam.categories c ON p.category_id = c.category_id
        JOIN sansam.file_management fm ON p.file_management_id = fm.file_management_id
        JOIN sansam.file_details fd ON fm.file_management_id = fd.file_management_id
        LEFT JOIN sansam.wishes w ON p.product_id = w.product_id
        WHERE fd.is_main = true
        GROUP BY p.product_id, p.product_name, p.price, p.brand_name, 
                 c.big_name, c.middle_name, c.small_name, 
                 fd.url, p.created_at, p.view_count
        """;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        log.info("DB rows to index: {}", rows.size());

        int batchSize = 5000;
        int counter = 0;
        BulkRequest.Builder br = new BulkRequest.Builder();

        for (Map<String, Object> row : rows) {
            Long productId = ((Number) row.get("product_id")).longValue();

            Object createdAtObj = row.get("created_at");
            LocalDateTime createdAt = null;
            if (createdAtObj instanceof Timestamp ts) {
                createdAt = ts.toLocalDateTime();
            } else if (createdAtObj instanceof LocalDateTime ldt) {
                createdAt = ldt;
            }

            Long wishCount = ((Number) row.get("wish_count")).longValue();

            ProductDoc doc = ProductDoc.builder()
                    .productId(productId)
                    .productName((String) row.get("product_name"))
                    .brand((String) row.get("brand_name"))
                    .price(((Number) row.get("price")).longValue())
                    .url((String) row.get("url"))
                    .bigCategory((String) row.get("big"))
                    .middleCategory((String) row.get("middle"))
                    .smallCategory((String) row.get("small"))
                    .viewCount(((Number) row.get("view_count")).longValue())
                    .createdAt(createdAt)
                    .wishCount(wishCount)
                    .build();

            br.operations(op -> op.index(idx -> idx
                    .index("products")
                    .id(doc.getProductId().toString())
                    .document(doc)
            ));
            counter++;

            if (counter % batchSize == 0) {
                executeBulk(br, counter);
                br = new BulkRequest.Builder();
            }
        }

        if (counter % batchSize != 0) {
            executeBulk(br, counter);
        }

        log.info("Bulk sync completed. Total indexed: {}", counter);
    }

    private void executeBulk(BulkRequest.Builder br, int counter) throws IOException {
        BulkResponse bulk = esClient.bulk(br.build());

        if (bulk.errors()) {
            bulk.items().forEach(item -> {
                if (item.error() != null) {
                    log.error("Bulk fail id={} reason={}", item.id(), item.error().reason());
                }
            });
        } else {
            log.info("Bulk executed successfully for {} documents", counter);
        }
    }

}

