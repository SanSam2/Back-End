package org.example.sansam.cart.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddCartRequest {
    private Long userId;
    private List<AddCartItem> addCartItems;
}
