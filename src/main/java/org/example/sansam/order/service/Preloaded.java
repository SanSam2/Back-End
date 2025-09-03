package org.example.sansam.order.service;

import org.example.sansam.product.domain.Product;
import org.example.sansam.status.domain.Status;
import org.example.sansam.user.domain.User;

import java.util.Map;

public record Preloaded(
        User user,
        Map<Long, Product> productMap,
        Status waiting,
        Status opWaiting
) {}
