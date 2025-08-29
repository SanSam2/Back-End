//package org.example.sansam.domain.product;
//
//import org.example.sansam.product.domain.Product;
//import org.example.sansam.product.domain.ProductDetail;
//import org.example.sansam.product.repository.ProductDetailJpaRepository;
//import org.example.sansam.product.repository.ProductJpaRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.jdbc.Sql;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@ActiveProfiles("test")
//@SpringBootTest
//public class ProductDetailRepositoryTest {
//    @Autowired
//    private ProductDetailJpaRepository productDetailJpaRepository;
//
//    @Autowired
//    private ProductJpaRepository productJpaRepository;
//
//    @DisplayName("상품 ID로 상품의 옵션을 조회할 수 있다.")
//    @Transactional
//    @Test
//    void findProductDetailByProductId() {
//        //given
//        Product product = productJpaRepository.findById(2L).orElseThrow();
//        //when
//        List<ProductDetail> productDetails = productDetailJpaRepository.findByProduct(product);
//        //then
//        assertThat(productDetails).hasSize(10);
//        assertThat(productDetails.getFirst().getProduct().getProductName()).isEqualTo(product.getProductName());
//        assertThat(productDetails.getLast().getMapName()).isEqualTo("BLACK-XS");
//       // System.out.println(productDetails.getFirst().getMapName() + "  " + productDetails.getFirst().getProduct().getProductName());
//    }
//
//
//}
