package org.example.sansam.wish.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DeleteWishRequest {
    private List<DeleteWishItem> deleteWishItemList;
}
