package org.example.sansam.cart.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DeleteCartRequest {
    private Long userId;
    private List<deleteCartItem> deleteCartItems;
}
