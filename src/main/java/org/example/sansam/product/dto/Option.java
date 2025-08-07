package org.example.sansam.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Option {
    private String color;
    private String size;

    public Option(String color, String size) {
        this.color = color;
        this.size = size;
    }
}
