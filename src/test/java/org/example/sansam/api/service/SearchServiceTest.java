//package org.example.sansam.api.service;
//
//import org.example.sansam.product.domain.Product;
//import org.example.sansam.product.repository.ProductJpaRepository;
//import org.example.sansam.product.service.ProductService;
//import org.example.sansam.search.dto.SearchItemResponse;
//import org.example.sansam.search.dto.SearchListResponse;
//import org.example.sansam.search.service.SearchService;
//import org.example.sansam.user.domain.Role;
//import org.example.sansam.user.domain.User;
//import org.example.sansam.user.repository.UserRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Sort;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.awt.print.Pageable;
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//
//@ActiveProfiles("test")
//@SpringBootTest
//public class SearchServiceTest {
//    @Autowired
//    private SearchService searchService;
//
//    @Autowired
//    private ProductService productService;
//
//    @Autowired
//    private ProductJpaRepository productJpaRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @DisplayName("유저가 로그인 후 검색창에 입력한 키워드에 해당하는 상품 리스트를 가져온다. ")
//    @Test
//    void searchProductListByKeyword() {
//        //given
//        String keyword = "1";
//        User newUser = createUser();
//        int page = 0;
//        int size = 10;
//        String sort = "createdAt";
//
//        //when
//        Page<SearchItemResponse> productList = searchService.searchProductListByKeyword(keyword, newUser.getId(), page, size, sort);
//
//        //then
//        assertThat(productList.getTotalElements()).isEqualTo(12);
//        assertThat(productList.getContent().getFirst().getProductId()).isEqualTo(20);
//        assertThat(productList.getContent().getFirst().isWish()).isEqualTo(false);
//        assertThat(productList.getContent().getFirst().getUrl()).isEqualTo("https://example.com/images/test_product_19.jpg");
//    }
//
//    @DisplayName("유저가 로그인 후 검색창에 입력한 키워드에 해당하는 상품 리스트를 가져온다. ")
//    @Test
//    void searchProductListByKeywordExistUser() {
//        //given
//        String keyword = "1";
//        User user = userRepository.findById(1L).orElseThrow();
//        int page = 0;
//        int size = 10;
//        String sort = "createdAt";
//
//        //when
//        Page<SearchItemResponse> productList = searchService.searchProductListByKeyword(keyword, user.getId(), page, size, sort);
//
//        //then
//        assertThat(productList.getTotalElements()).isEqualTo(12);
//        assertThat(productList.getContent().getFirst().getProductId()).isEqualTo(20);
//        assertThat(productList.getContent().getFirst().isWish()).isEqualTo(false);
//        assertThat(productList.getContent().getFirst().getUrl()).isEqualTo("https://example.com/images/test_product_19.jpg");
//    }
//
//    @DisplayName("유저가 로그인을 하지 않고 검색창에 입력한 키워드에 해당하는 상품 리스트를 가져온다. ")
//    @Test
//    void searchProductListByKeywordNotLogin() {
//        //given
//        String keyword = "1";
//        int page = 0;
//        int size = 10;
//        String sort = "createdAt";
//
//        //when
//        Page<SearchItemResponse> productList = searchService.searchProductListByKeyword(keyword, null, page, size, sort);
//
//        //then
//        assertThat(productList.getTotalElements()).isEqualTo(12);
//        assertThat(productList.getContent().getFirst().getProductId()).isEqualTo(20);
//        assertThat(productList.getContent().getFirst().isWish()).isEqualTo(false);
//        assertThat(productList.getContent().getFirst().getUrl()).isEqualTo("https://example.com/images/test_product_19.jpg");
//    }
//
//    @DisplayName("유저가 로그인 후, 선택한 카테고리에 해당하는 상품 리스트를 가져온다. ")
//    @Test
//    void searchProductListByCategoryName() {
//        //given
//        String big = "여성";
//        String middle = "하의";
//        String small = "긴바지";
//        User newUser = userRepository.findById(1L).orElseThrow();
//        int page = 0;
//        int size = 10;
//        String sort = "price";
//
//        //when
//        Page<SearchItemResponse> productList = searchService.searchProductListByCategory(big, middle, small, newUser.getId(), page, size, sort);
//
//        //then
//        assertThat(productList.getTotalElements()).isEqualTo(7);
//        assertThat(productList.getContent().getFirst().getProductId()).isEqualTo(4);
//        assertThat(productList.getContent().getFirst().isWish()).isEqualTo(true);
//    }
//
//
//    @DisplayName("유저가 로그인을 하지 않고, 선택한 카테고리에 해당하는 상품 리스트를 가져온다. ")
//    @Test
//    void searchProductListByCategoryNameNotLogin() {
//        //given
//        String big = "여성";
//        String middle = "하의";
//        String small = "긴바지";
//        int page = 0;
//        int size = 10;
//        String sort = "createdAt";
//
//        //when
//        Page<SearchItemResponse> productList = searchService.searchProductListByCategory(big, middle, small, null, page, size, sort);
//
//        //then
//        assertThat(productList.getTotalElements()).isEqualTo(7);
//        assertThat(productList.getContent().getFirst().getProductId()).isEqualTo(28);
//        assertThat(productList.getContent().getFirst().isWish()).isEqualTo(false);
//    }
//
//    private static User createUser() {
//        return User.builder()
//                .email("1234@gmail.com")
//                .password("pw1234")
//                .name("dmsrud")
//                .mobileNumber("01022223333")
//                .salary(1000L)
//                .emailAgree(true)
//                .activated(true)
//                .createdAt(LocalDateTime.now())
//                .role(Role.valueOf("USER"))
//                .build();
//    }
//
//
//    @DisplayName("유저 ID가 존재하지 않을 때,선택한 카테고리에 해당하는 상품 리스트를 가져온다. ")
//    @Test
//    void searchProductListByCategoryNameNotExist() {
//        //given
//        String big = "여성";
//        String middle = "하의";
//        String small = "긴바지";
//        int page = 0;
//        int size = 10;
//        String sort = "createdAt";
//
//        //when
//        Page<SearchItemResponse> productList = searchService.searchProductListByCategory(big, middle, small, 3L, page, size, sort);
//
//        //then
//        assertThat(productList.getTotalElements()).isEqualTo(7);
//        assertThat(productList.getContent().getFirst().getProductId()).isEqualTo(28);
//        assertThat(productList.getContent().getFirst().isWish()).isEqualTo(false);
//    }
//
//    @DisplayName("상품을 상품 DTO로 변환한다. ")
//    @Test
//    void productToDto() {
//        //given
//        List<Product> products = productJpaRepository.findAll();
//        User user = userRepository.findById(1L).orElseThrow();
//
//        //when
//        List<SearchListResponse> productResponses = searchService.productToDto(products, user.getId());
//
//        //then
//        assertThat(productResponses.size()).isEqualTo(30);
//        assertThat(productResponses.getFirst().isWish()).isEqualTo(false);
//        assertThat(productResponses.getFirst().getProductId()).isEqualTo(1L);
//        assertThat(productResponses.getLast().getProductId()).isEqualTo(30L);
//    }
//
//    @DisplayName("메인화면에서 위시가 많은 상품순으로 상품 리스트를 10개 가져온다. ")
//    @Test
//    void getProductsByLike() {
//        //given
//        User user = userRepository.findById(1L).orElseThrow();
//
//        //when
//        List<SearchListResponse> productResponses = searchService.getProductsByLike(user.getId());
//        //then
//        assertThat(productResponses.size()).isEqualTo(10);
//        assertThat(productResponses.getFirst().getProductId()).isEqualTo(4L);
//        assertThat(productResponses.get(1).getProductId()).isEqualTo(28L);
//        assertThat(productResponses.getFirst().isWish()).isEqualTo(true);
//        assertThat(productResponses.getLast().isWish()).isEqualTo(false);
//    }
//
//    @DisplayName("메인화면에서 추천 상품의 리스트를 10개 가져올 때, 유저가 로그인을 하지 않았을 때는 조회순으로 상품 10개가 반환된다. ")
//    @Test
//    void getProductsByRecommendNotLogin() {
//        //given
//        //when
//        List<SearchListResponse> productResponses = searchService.getProductsByRecommend(null);
//        //then
//        assertThat(productResponses.size()).isEqualTo(10);
//        assertThat(productResponses.getFirst().getProductId()).isEqualTo(29L);
//        assertThat(productResponses.get(1).getProductId()).isEqualTo(23L);
//        assertThat(productResponses.getFirst().isWish()).isEqualTo(false);
//        assertThat(productResponses.getLast().isWish()).isEqualTo(false);
//    }
//
//
//    @DisplayName("메인화면에서 추천 상품의 리스트를 10개 가져올 때, 유저의 위시리스트에 상품이 없으면 기본 조회수순으로 상품 10개가 반환된다. ")
//    @Test
//    void getProductsByRecommendUserNoneWish() {
//        //given
//        User user = userRepository.findById(3L).orElseThrow();
//        //when
//        List<SearchListResponse> productResponses = searchService.getProductsByRecommend(user.getId());
//        //then
//        assertThat(productResponses.size()).isEqualTo(10);
//        assertThat(productResponses.getFirst().getProductId()).isEqualTo(29L);
//        assertThat(productResponses.get(1).getProductId()).isEqualTo(23L);
//        assertThat(productResponses.getFirst().isWish()).isEqualTo(false);
//        assertThat(productResponses.getLast().isWish()).isEqualTo(false);
//    }
//
//    @DisplayName("메인화면에서 추천 상품의 리스트를 10개 가져올 때, 유저의 위시리스트에 상품이 있으면 같은 카테고리의 상품 10개가 반환된다. ")
//    @Test
//    void getProductsByRecommendUserExist() {
//        //given
//        User user = userRepository.findById(2L).orElseThrow();
//        //when
//        List<SearchListResponse> productResponses = searchService.getProductsByRecommend(user.getId());
//        //then
//        assertThat(productResponses.size()).isEqualTo(10);
//        assertThat(productResponses.getFirst().getProductId()).isEqualTo(23L);
//        assertThat(productResponses.getLast().isWish()).isEqualTo(false);
//    }
//
//    @DisplayName("상품 정렬 기준에 따라 조회된 상품을 정렬한다.")
//    @Test
//    void getSort() {
//        //given
//        String sortKey = "viewCount";
//        //when
//        Sort sort = searchService.getSort(sortKey);
//        //then
//        assertThat(sort.getOrderFor(sortKey).getDirection()).isEqualTo(Sort.Direction.DESC);
//        assertThat(sort.getOrderFor(sortKey).toString()).isEqualTo("viewCount: DESC");
//    }
//}
