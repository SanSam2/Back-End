package org.example.sansam.search.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Getter
@Document(indexName = "products", createIndex = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchProductDocument {

    @Id
    private Long productId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String productName;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String brand;

    @Field(type = FieldType.Long)
    private Long price;

    @Field(type = FieldType.Keyword, index = false)
    private String url;

    @Field(type = FieldType.Keyword)
    private String bigCategory;

    @Field(type = FieldType.Keyword)
    private String middleCategory;

    @Field(type = FieldType.Keyword)
    private String smallCategory;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Field(type = FieldType.Long)
    private Long viewCount;

    @Field(type = FieldType.Long)
    private Long wishCount;
}

