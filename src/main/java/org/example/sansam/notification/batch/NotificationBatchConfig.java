package org.example.sansam.notification.batch;


import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.sansam.notification.domain.NotificationHistories;
import org.example.sansam.notification.domain.NotificationType;
import org.example.sansam.notification.dto.NotificationDTO;
import org.example.sansam.notification.event.sse.NotificationSavedEvent;
import org.example.sansam.notification.event.sse.ReviewRequestEvent;
import org.example.sansam.notification.repository.NotificationHistoriesRepository;
import org.example.sansam.notification.service.NotificationService;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.repository.OrderRepository;
import org.example.sansam.review.repository.ReviewJpaRepository;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.repository.UserRepository;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.example.sansam.status.domain.StatusEnum.ORDER_PAID;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Log4j2
public class NotificationBatchConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final NotificationHistoriesRepository notificationHistoryRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ReviewJpaRepository reviewRepository;
    private final NotificationService notificationService;

    private static final String EVENT_NAME = NotificationType.BROADCAST.getEventName();

    /** ================== 만료 알림 삭제 Job ================== **/
    @Bean
    public Job deleteExpiredNotificationsJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("deleteExpiredNotificationsJob", jobRepository)
                .start(deleteExpiredNotificationsStep(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Step deleteExpiredNotificationsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("deleteExpiredNotificationsStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    int deletedCount = notificationHistoryRepository
                            .deleteByExpiredAtBefore(LocalDateTime.now());
                    log.info("삭제된 만료 알림 수: {}", deletedCount);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    /** ================== 리뷰 요청 Job ================== **/

//    @Bean
//    public Job reviewRequestJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
//        return new JobBuilder("reviewRequestJob", jobRepository)
//                .start(reviewRequestStep(jobRepository, transactionManager))
//                .build();
//    }
//
//    @Bean
//    public Step reviewRequestStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
//        return new StepBuilder("reviewRequestStep", jobRepository)
//                .<Order, Order>chunk(500, transactionManager) // 주문 500건 단위 처리
//                .reader(reviewRequestReader())
//                .processor(reviewRequestProcessor())
//                .writer(reviewRequestWriter())
//                .build();
//    }
//
//    // fetch join 으로 쿼리 한개에 다 가져오기.
//    @Bean
//    public JpaPagingItemReader<Order> reviewRequestReader() {
//        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
//
//        return new JpaPagingItemReaderBuilder<Order>()
//                .name("reviewRequestReader")
//                .entityManagerFactory(entityManagerFactory)
//                .queryString(
//                        "SELECT DISTINCT o FROM Order o " +
//                                "JOIN FETCH o.orderProducts op " +
//                                "JOIN FETCH op.product p " +
//                                "WHERE o.deliveredAt < :oneDayAgo " +
//                                "AND o.status.statusId = :statusId"
//                )
//                .parameterValues(Map.of("oneDayAgo", oneDayAgo, "statusId", ORDER_PAID))
//                .pageSize(500)
//                .build();
//    }
//
//    @Bean
//    public ItemProcessor<Order, Order> reviewRequestProcessor() {
//        return order -> {
//            Long userId = order.getUser().getId();
//
//            List<Long> productIds = order.getOrderProducts().stream()
//                    .map(op -> op.getProduct().getId())
//                    .toList();
//
//            List<Long> reviewedProductIds =
//                    reviewRepository.findReviewedProductIdsByUserAndProducts(userId, productIds);
//
//            boolean hasUnreviewedProduct = productIds.stream()
//                    .anyMatch(pid -> !reviewedProductIds.contains(pid));
//
//            return hasUnreviewedProduct ? order : null; // 리뷰 안 한 경우만 Writer로 넘김
//        };
//    }
//
//    @Bean
//    public ItemWriter<Order> reviewRequestWriter() {
//        return orders -> {
//            for (Order order : orders) {
//                eventPublisher.publishEvent(new ReviewRequestEvent(order.getUser(), order.getOrderName()));
//            }
//        };
//    }
}
