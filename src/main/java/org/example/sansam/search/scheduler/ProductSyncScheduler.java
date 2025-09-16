package org.example.sansam.search.scheduler;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.product.domain.Category;
import org.example.sansam.product.domain.Product;
import org.example.sansam.product.repository.ProductJpaRepository;
import org.example.sansam.search.dto.ProductDoc;
import org.example.sansam.wish.repository.WishJpaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSyncScheduler {

    private final ElasticsearchClient esClient;
    private final ProductJpaRepository productJpaRepository;
    private final WishJpaRepository wishJpaRepository;

    // 변경된 상품 ID 모아두는 Set
    private final Set<Long> changedProductIds = ConcurrentHashMap.newKeySet();

    public void updateProductData(Long productId) {
        changedProductIds.add(productId);
        log.debug("[updateProductData] productId={} added, total changed={}", productId, changedProductIds.size());
    }

    @Transactional
    @Scheduled(fixedRate = 300_000) // 5분마다 실행
    public void syncProductsToES() throws IOException {
        BulkRequest.Builder br = new BulkRequest.Builder();
        int opCount = 0;

        LocalDateTime referenceTime = LocalDateTime.now().minusMinutes(5);
        List<Product> addProducts = productJpaRepository.findAfterCreatedAt(referenceTime);

        log.info("[syncProductsToES] 신규 상품 {}개 색인 예정", addProducts.size());

        for (Product product : addProducts) {
            Long wishCount = wishJpaRepository.countByProductId(product.getId());
            Category category = product.getCategory();

            ProductDoc doc = ProductDoc.builder()
                    .productId(product.getId())
                    .productName(product.getProductName())
                    .brand(product.getBrandName())
                    .price(product.getPrice())
                    .url(product.getFileManagement().getFileUrl())
                    .bigCategory(category.getBigName())
                    .middleCategory(category.getMiddleName())
                    .smallCategory(category.getSmallName())
                    .viewCount(product.getViewCount())
                    .createdAt(product.getCreatedAt())
                    .wishCount(wishCount)
                    .build();

            br.operations(op -> op.index(idx -> idx
                    .index("products")
                    .id(doc.getProductId().toString())
                    .document(doc)
            ));
            opCount++;
        }

        Set<Long> idsToSync = new HashSet<>(changedProductIds);

        log.info("[syncProductsToES] 업데이트 대상 상품 {}개", idsToSync.size());

        for (Long productId : idsToSync) {
            productJpaRepository.findById(productId).ifPresent(product -> {
                Long wishCount = wishJpaRepository.countByProductId(productId);

                Map<String, Object> fieldsToUpdate = Map.of(
                        "viewCount", product.getViewCount(),
                        "wishCount", wishCount
                );

                log.debug(" → productId={} viewCount={} wishCount={}",
                        productId, product.getViewCount(), wishCount);

                br.operations(op -> op.update(upd -> upd
                        .index("products")
                        .id(productId.toString())
                        .action(a -> a.doc(fieldsToUpdate).docAsUpsert(true)) // 없는 문서면 upsert
                ));
            });
            opCount++;
        }

        if (opCount > 0) {
            BulkRequest bulkRequest = br.build();

            BulkResponse response = esClient.bulk(bulkRequest);
            log.info("[syncProductsToES] Synced {} operations to Elasticsearch", opCount);

            if (response.errors()) {
                response.items().forEach(item -> {
                    if (item.error() != null) {
                        log.error("[syncProductsToES] Bulk fail: id={} reason={}",
                                item.id(), item.error().reason());
                    }
                });
            }

            changedProductIds.removeAll(idsToSync);

        } else {
            log.info("[syncProductsToES] No products to sync this cycle");
        }
    }
}

