//package org.example.sansam.domain.product;
//
//import org.example.sansam.product.domain.ProductConnect;
//import org.example.sansam.product.domain.ProductOption;
//import org.example.sansam.product.repository.ProductConnectJpaRepository;
//import org.example.sansam.product.repository.ProductOptionJpaRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.jdbc.Sql;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@ActiveProfiles("test")
//@SpringBootTest
//public class ProductConnectRepositoryTest {
//    @Autowired
//    private ProductConnectJpaRepository productConnectJpaRepository;
//
//    @DisplayName("상품 디테일 ID로 상품의 각 옵션을 찾을 수 있다.")
//    @Test
//    void findProductOptionsByProductDetailId() {
//        //given
//        Long productDetailId = 3L;
//
//        //when
//        List<ProductOption> productOptions = productConnectJpaRepository.findOptionsByProductDetailId(productDetailId);
//
//        //then
//        assertThat(productOptions).hasSize(2);
//        assertThat(productOptions.get(0).getType()).isEqualTo("color");
//        assertThat(productOptions.get(0).getName()).isEqualTo("WHITE");
//        assertThat(productOptions.get(1).getType()).isEqualTo("size");
//        assertThat(productOptions.get(1).getName()).isEqualTo("L");
//    }
//
//
//}
