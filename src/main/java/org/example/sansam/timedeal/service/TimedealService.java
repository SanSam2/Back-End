package org.example.sansam.timedeal.service;

import lombok.AllArgsConstructor;
import org.example.sansam.product.domain.Product;
import org.example.sansam.product.domain.ProductDetail;
import org.example.sansam.product.dto.ProductDetailResponse;
import org.example.sansam.timedeal.domain.Timedeal;
import org.example.sansam.timedeal.dto.TimeDealDetailResponse;
import org.example.sansam.timedeal.dto.TimeDealResponse;
import org.example.sansam.timedeal.repository.TimedealJpaRepositiry;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class TimedealService {
    private final TimedealJpaRepositiry timedealJpaRepositiry;

    //현재 날짜 기준으로, 오늘 날짜, 내일 날짜 타임딜 리스트 가져오기
    public List<TimeDealResponse> getTimeDeals() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atTime(LocalTime.MAX);
        List<Timedeal> timedeals = timedealJpaRepositiry.findByStartAtBetween(start, end);
        List<TimeDealResponse> timeDealResponseList = timedeals.stream().map(
                timedeal -> {
                    Product p = timedeal.getProductDetail().getProduct();
                    return TimeDealResponse.builder()
                            .productId(p.getId())
                            .productName(p.getProductName())
                            .productImage(p.getFileManagement().getFileUrl())
                            .status(timedeal.getStatus().getStatusName())
                            .originalPrice(p.getPrice())
                            .timeDealPrice(timedeal.getPrice())
                            .startAt(timedeal.getStartAt())
                            .endAt(timedeal.getEndAt())
                            .build();
                }
        ).collect(Collectors.toList());
        return timeDealResponseList;
    }

//    public TimeDealDetailResponse getTimedealDetail(Long productId, LocalDateTime startAt) {
//        Timedeal timedeal = timedealJpaRepositiry.findByStartAt(startAt, productId);
//        Product product = timedeal.getProductDetail().getProduct();
//        ProductDetail productDetail = timedeal.getProductDetail();
//
//        ProductDetailResponse detailResponse = timedeal.getProductDetail()
//        TimeDealDetailResponse response = TimeDealDetailResponse.builder()
//                .productId(product.getId())
//                .productName(product.getProductName())
//                .categoryName(product.getCategory().toString())
//                .brandName(product.getBrandName())
//                .price(product.getPrice())
//                .description(product.getDescription())
//                .imageUrl(timedeal.getProductDetail().getFileManagement().getFileUrl())
//                .detailResponse(timedeal.getProductDetail())
//                .timeDealPrice(timedeal.getPrice())
//                .timeDealStatus(timedeal.getStatus().getStatusName())
//                .endAt(timedeal.getEndAt())
//                .build();
//    }

}
