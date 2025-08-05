package org.example.sansam.notification.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.sansam.notification.event.ReviewRequestEvent;
import org.example.sansam.notification.repository.NotificationHistoriesRepository;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.repository.OrderRepository;
import org.example.sansam.review.repository.ReviewJpaRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {
    private final NotificationHistoriesRepository notificationHistoryRepository;
    private final OrderRepository orderRepository;
    private final ReviewJpaRepository reviewRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시에 만료 알림 삭제
    public void deleteExpiredNotification() {
        int deleted = notificationHistoryRepository.deleteByExpiredAtBefore(Timestamp.valueOf(LocalDateTime.now()));
        System.out.println("삭제된 만료 알림 수: " + deleted);
    }

//    @Scheduled(cron = "0 0 14 * * *") // 매 시간 정각
//    public void checkReviewRequestCondition() {
//        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
//
//        // 배송 완료 상태 && delivered_at 기준 하루 이상 지난 주문 조회
//        List<Order> deliveredOrders = orderRepository.findByDeliveredAtBeforeAndStatus_statusName(oneDayAgo, "배송 완료");
//
//        for (Order order : deliveredOrders) {
//            Long userId = order.getUser().getId();
//
//            // 주문에 포함된 상품들 중 리뷰가 작성되지 않은 상품이 하나라도 있는지 확인
//            boolean hasUnreviewedProduct = order.getOrderProducts().stream()
//                    .map(op -> op.getProduct().getProductsId())
//                    .anyMatch(productId -> !reviewRepository.existsByUserIdAndProductId(userId, productId));
//
//            if (!hasUnreviewedProduct) {
//                continue; // 리뷰가 다 작성되었으면 알림 X
//            }
//
//            // 이벤트 발행
//            eventPublisher.publishEvent(new ReviewRequestEvent(order.getUser(), order.getOrderName()));
//        }
//
//    }
}
