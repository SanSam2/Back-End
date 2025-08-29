//package org.example.sansam.domain.product;
//
//import org.example.sansam.product.domain.Product;
//import org.example.sansam.product.repository.ProductJpaRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.jdbc.Sql;
//
//
//import java.time.LocalDateTime;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
//@ActiveProfiles("test")
//@SpringBootTest
////@Sql(scripts = "classpath:testdata/insert_products.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
//class ProductRepositoryTest {
//
//    @Autowired
//    private ProductJpaRepository productJpaRepository;
//
//    @DisplayName("검색창에 키워드를 입력하여 상품을 조회한다.")
//    //@Sql(scripts = "classpath:testdata/insert_products.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
//    @Test
//    void findProductsByKeyword() {
//        //given
//        String keyword = "10";
//        Pageable pageable = PageRequest.of(0, 10);
//        //when
//        Page<Product> products = productJpaRepository.findByKeyword(keyword, pageable);
//        //then
//        assertThat(products.getTotalElements()).isEqualTo(1);
//        assertThat(products.getContent().get(0).getProductName()).isEqualTo("테스트상품_10");// 데이터보고 개수 설정
//    }
//
//    @DisplayName("검색창에 키워드를 입력하여, 일치하는 카테고리의 상품을 조회한다.")
//    @Test
//    void findProductsInCategoryByKeyword() {
//        //given
//        String keyword = "긴팔";
//        Pageable pageable = PageRequest.of(0, 10);
//        //whe
//        Page<Product> products = productJpaRepository.findByKeyword(keyword, pageable);
//        //then
//        assertThat(products.getTotalElements()).isEqualTo(8);
//    }
//
//    @DisplayName("검색창에 빈 키워드를 입력하여 상품을 조회하면, 전체 상품이 반환된다.")
//    @Test
//    void findProductsByEmptyKeyword() {
//        //given
//        String keyword = "";
//        Pageable pageable = PageRequest.of(0, 10);
//        //when
//        Page<Product> products = productJpaRepository.findByKeyword(keyword, pageable);
//        //then
//        assertThat(products.getTotalElements()).isEqualTo(30); // 데이터보고 개수 설정
//    }
//
//    @DisplayName("검색창에 키워드 입력하여 상품을 조회할 때, DB에 키워드에 해당하는 상품명, 카테고리를 가진 상품이 없을 때 빈 리스트가 반환된다.")
//    @Test
//    void findProductsByNotExistKeyword() {
//        //given
//        String keyword = "산삼";
//        Pageable pageable = PageRequest.of(0, 10);
//        //when
//        Page<Product> products = productJpaRepository.findByKeyword(keyword, pageable);
//        //then
//        assertThat(products).hasSize(0);
//        // 데이터보고 개수 설정
//    }
//
//    @DisplayName("카테고리 대, 중, 소 이름으로 해당 카테고리에 해당하는 상품을 조회한다.")
//    @Test
//    void findProductsByCategoryName() {
//        //given
//        String big = "여성";
//        String middle = "상의";
//        String small = "긴팔";
//        Pageable pageable = PageRequest.of(0, 10);
//        //when
//        Page<Product> products = productJpaRepository.findByCategoryNames(big, middle, small, pageable);
//        //then
//        assertThat(products).hasSize(8);
//    }
//
//    @DisplayName("카테고리 이름으로 조회를 할 때, 존재하지 않는 카테고리로 조회를 하면 빈 리스트를 반환한다.")
//    @Test
//    void findProductsByNotExistCategoryName() {
//        //given
//        String big = "여성";
//        String middle = "상의";
//        String small = "조끼";
//        Pageable pageable = PageRequest.of(0, 10);
//        //when
//        Page<Product> products = productJpaRepository.findByCategoryNames(big, middle, small, pageable);
//        //then
//        assertThat(products.getContent()).isEmpty();
//        assertThat(products.getContent()).hasSize(0);
//    }
//
//    //이거 ASC, DESC 이거 맞는지 확인하기
//    private Sort getSort(String sortKey) {
//        switch (sortKey) {
//            case "price":
//                return Sort.by(Sort.Direction.ASC, "price");
//            case "viewCount":
//                return Sort.by(Sort.Direction.DESC, "viewCount");
//            case "createdAt":
//            default:
//                return Sort.by(Sort.Direction.DESC, "createdAt");
//        }
//    }
//
//    @DisplayName("검색 결과 조회된 상품을 최신순, 낮은 가격순, 조회수순으로 정렬할 수 있다. ")
//    @Test
//    void findSortedProducts() {
//        //given
//        String keyword = "";
//        String sortBy = "createdAt";
//        Pageable pageable = PageRequest.of(0, 10, getSort(sortBy)); //price, viewCount, createdAt
//        //when
//        Page<Product> products = productJpaRepository.findByKeyword(keyword, pageable);
//        //then
//        assertThat(products.getTotalElements()).isEqualTo(30);
//        assertThat(products.getContent().get(0).getProductName()).isEqualTo("테스트상품_29"); //이거 바꾸기
//    }
//
//    @DisplayName("검색된 결과를 정렬할 때 잘못된 정렬 기준을 넣었을 때, 기본 값인 최신순으로 정렬된다.")
//    @Test
//    void findWrongSortedProducts() {
//        //given
//        String keyword = "";
//        String sortBy = "price2";
//        Pageable pageable = PageRequest.of(0, 10, getSort(sortBy)); //price, viewCount, createdAt
//        //when
//        Page<Product> products = productJpaRepository.findByKeyword(keyword, pageable);
//        //then
//        assertThat(products.getTotalElements()).isEqualTo(30);
//        assertThat(products.getContent().get(0).getProductName()).isEqualTo("테스트상품_29");
//    }
//
//    @DisplayName("조회된 결과에서 상픔 클릭 시 상품 ID로 상품 정보를 조회할 수 있다")
//    @Test
//    void findProductById() {
//        //given
//        //when
//        Product product = productJpaRepository.findById(2L).orElseThrow();
//        //then
//        assertThat(product).isNotNull();
//        assertThat(product.getProductName()).isEqualTo("테스트상품_1");
//    }
//
//    @DisplayName("상품 ID로 상품 정보를 조회할 때, 해당하는 상품이 존재하지 않으면 오류가 발생한다.")
//    @Test
//    void findProductByNotExistId() {
//        //given
//        //when
//        //then
//        assertThatThrownBy(() ->
//                productJpaRepository.findById(31L)
//                        .orElseThrow(() -> new IllegalArgumentException("해당 ID를 가진 상품이 존재하지 않습니다."))
//        )
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessage("해당 ID를 가진 상품이 존재하지 않습니다.");
//
//    }
//
////    @DisplayName("원하는 판매상태를 가진 상품들을 조회한다.")
////    @Test
////    void findAllBySellingStatusIn() {
////        // given
////        Product product1 = createProduct("001", HANDMADE, SELLING, "아메리카노", 4000);
////        Product product2 = createProduct("002", HANDMADE, HOLD, "카페라떼", 4500);
////        Product product3 = createProduct("003", HANDMADE, STOP_SELLING, "팥빙수", 7000);
////        productRepository.saveAll(List.of(product1, product2, product3));
////
////        // when
////        List<Product> products = productRepository.findAllBySellingStatusIn(List.of(SELLING, HOLD));
////
////        // then
////        assertThat(products).hasSize(2)
////                .extracting("productNumber", "name", "sellingStatus")
////                .containsExactlyInAnyOrder(
////                        tuple("001", "아메리카노", SELLING),
////                        tuple("002", "카페라떼", HOLD)
////                );
////    }
//
////    assertThatThrownBy(() -> cafeKiosk.createOrder(LocalDateTime.of(2023, 1, 17, 9, 59)))
////            .isInstanceOf(IllegalArgumentException.class)
////            .hasMessage("주문 시간이 아닙니다. 관리자에게 문의하세요.");
//}