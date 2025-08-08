package org.example.sansam.notification.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.sansam.product.domain.ProductDetail;

@Getter
@RequiredArgsConstructor
public class ProductQuantityLowEvent {
    private final ProductDetail productDetail;
}
