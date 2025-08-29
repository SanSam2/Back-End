package org.example.sansam.search.controller;

import lombok.RequiredArgsConstructor;
import org.example.sansam.search.service.ProductSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/es")
public class ProductSyncController {

    private final ProductSyncService bulkSyncService;

    @PostMapping("/sync")
    public ResponseEntity<String> syncProducts() {
        try {
            bulkSyncService.bulkSyncAllProducts();
            return ResponseEntity.ok("Products synced to Elasticsearch!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Sync failed: " + e.getMessage());
        }
    }
}