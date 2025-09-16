package org.example.sansam.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.search.dto.ProductDoc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

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
        FROM sansam_local.products p
        JOIN sansam_local.categories c ON p.category_id = c.category_id
        JOIN sansam_local.file_management fm ON p.file_management_id = fm.file_management_id
        JOIN sansam_local.file_details fd ON fm.file_management_id = fd.file_management_id
        LEFT JOIN sansam_local.wishes w ON p.product_id = w.product_id
        WHERE fd.is_main = true
        GROUP BY p.product_id, p.product_name, p.price, p.brand_name,
                 c.big_name, c.middle_name, c.small_name,
                 fd.url, p.created_at, p.view_count
        """;

        esClient.indices().putSettings(s -> s
                .index("products")
                .settings(st -> st.refreshInterval(Time.of(t -> t.time("-1"))))
        );
        log.info("Index settings updated: refresh_interval = -1");

        int batchSize = 500;
        int counter = 0;

        try (Connection conn = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     ResultSet.TYPE_FORWARD_ONLY,
                     ResultSet.CONCUR_READ_ONLY)) {

            // MySQL streaming mode (핵심!)
            ps.setFetchSize(Integer.MIN_VALUE);

            try (ResultSet rs = ps.executeQuery()) {
                BulkRequest.Builder br = new BulkRequest.Builder();

                log.info("Start fetching rows from DB...");

                while (rs.next()) {
                    Long productId = rs.getLong("product_id");

                    LocalDateTime createdAt = null;
                    Object createdAtObj = rs.getObject("created_at");
                    if (createdAtObj instanceof Timestamp ts) {
                        createdAt = ts.toLocalDateTime();
                    } else if (createdAtObj instanceof LocalDateTime ldt) {
                        createdAt = ldt;
                    }

                    ProductDoc doc = ProductDoc.builder()
                            .productId(productId)
                            .productName(rs.getString("product_name"))
                            .brand(rs.getString("brand_name"))
                            .price(rs.getLong("price"))
                            .url(rs.getString("url"))
                            .bigCategory(rs.getString("big"))
                            .middleCategory(rs.getString("middle"))
                            .smallCategory(rs.getString("small"))
                            .viewCount(rs.getLong("view_count"))
                            .createdAt(createdAt)
                            .wishCount(rs.getLong("wish_count"))
                            .build();

                    br.operations(op -> op.index(idx -> idx
                            .index("products")
                            .id(doc.getProductId().toString())
                            .document(doc)
                    ));

                    counter++;

                    // 중간 로그
                    if (counter % 100_000 == 0) {
                        log.info("Fetched {} rows so far...", counter);
                    }

                    if (counter % batchSize == 0) {
                        executeBulk(br, counter);
                        br = new BulkRequest.Builder(); // 새 빌더 생성
                    }
                }

                if (counter % batchSize != 0) {
                    executeBulk(br, counter);
                }
            }

        } catch (Exception e) {
            log.error("DB streaming failed", e);
            throw new RuntimeException(e);
        }

        esClient.indices().putSettings(s -> s
                .index("products")
                .settings(st -> st.refreshInterval(Time.of(t -> t.time("1s"))))
        );
        log.info("Bulk sync completed. Total indexed: {}", counter);
    }

    private void executeBulk(BulkRequest.Builder br, int counter) throws IOException {
        BulkResponse response = esClient.bulk(br.build());
        if (response.errors()) {
            for (BulkResponseItem item : response.items()) {
                if (item.error() != null) {
                    log.error("Bulk error for id {}: {}", item.id(), item.error().reason());
                }
            }
            throw new RuntimeException("Bulk insert error at count " + counter);
        }
        log.info("Bulk executed up to count: {}", counter);
    }
}
