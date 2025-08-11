package org.example.sansam.global.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    // 기본 정보 설정
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SanSam API명세서")
                        .version("v1")
                        .description("SanSam의 Swagger OpenAPI 명세입니다."));
    }

    // 그룹별 API 문서 생성
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("user API")
                .pathsToMatch("/api/users/**")
                .build();
    }

    @Bean
    public GroupedOpenApi orderApi() {
        return GroupedOpenApi.builder()
                .group("주문 API")
                .pathsToMatch("/api/orders/**")
                .build();
    }

    @Bean
    public GroupedOpenApi payApi() {
        return GroupedOpenApi.builder()
                .group("결제 API")
                .pathsToMatch("/api/pay/**")
                .build();
    }

    @Bean
    public GroupedOpenApi orderFindApi() {
        return GroupedOpenApi.builder()
                .group("상품조회 API")
                .pathsToMatch("/orders/**")
                .build();
    }

    @Bean
    public GroupedOpenApi productApi() {
        return GroupedOpenApi.builder()
                .group("상품 API")
                .pathsToMatch("/api/products/**")
                .build();
    }

    @Bean
    public GroupedOpenApi timedealApi() {
        return GroupedOpenApi.builder()
                .group("타임딜 API")
                .pathsToMatch("/api/timedeals/**")
                .build();
    }

    @Bean
    public GroupedOpenApi reviewApi() {
        return GroupedOpenApi.builder()
                .group("리뷰 API")
                .pathsToMatch("/api/reviews/**")
                .build();
    }

    @Bean
    public GroupedOpenApi wishApi() {
        return GroupedOpenApi.builder()
                .group("위시 API")
                .pathsToMatch("/api/wishes/**")
                .build();
    }

    @Bean
    public GroupedOpenApi cartApi() {
        return GroupedOpenApi.builder()
                .group("장바구니 API")
                .pathsToMatch("/api/carts/**")
                .build();
    }

    @Bean
    public GroupedOpenApi searchApi() {
        return GroupedOpenApi.builder()
                .group("검색 API")
                .pathsToMatch("/api/search/**")
                .build();
    }

    @Bean
    public GroupedOpenApi chatApi() {
        return GroupedOpenApi.builder()
                .group("채팅 API")
                .pathsToMatch("/api/chatroom/**")
                .build();
    }

}
