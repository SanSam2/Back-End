package org.example.sansam.product.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="category_id")
    private Long id;

    @Column(name="big_name")
    private String bigName;

    @Column(name="middle_name")
    private String middleName;

    @Column(name="small_name")
    private String smallName;

    @Override
    public String toString() {
        return String.format("%s>%s>%s", bigName != null ? bigName : "", middleName != null ? middleName : "", smallName != null ? smallName : "");
    }

    public static String toCategoryString(String bigName, String middleName, String smallName) {
        return String.format("%s>%s>%s", bigName != null ? bigName : "", middleName != null ? middleName : "", smallName != null ? smallName : "");
    }
}
