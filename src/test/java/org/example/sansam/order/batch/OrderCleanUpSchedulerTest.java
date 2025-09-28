package org.example.sansam.order.batch;

import org.example.sansam.order.repository.OrderRepository;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.example.sansam.status.repository.StatusRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class OrderCleanUpSchedulerTest {

    @Mock
    OrderRepository orderRepository;
    @Mock
    StatusRepository statusRepository;

    @InjectMocks
    OrderCleanUpScheduler scheduler;

    @Test
    void cleanUpExpiredOrders_정상_호출되고_30분_기준시간으로_delete된다() {
        // given
        Status waiting = new Status(StatusEnum.ORDER_WAITING);
        given(statusRepository.findByStatusName(StatusEnum.ORDER_WAITING)).willReturn(waiting);
        given(orderRepository.deleteExpiredWaitingOrders(any(Status.class), any(LocalDateTime.class)))
                .willReturn(5);

        // when
        LocalDateTime lowerBound = LocalDateTime.now().minusMinutes(31);
        scheduler.cleanUpExpiredOrders();
        LocalDateTime upperBound = LocalDateTime.now().minusMinutes(29);

        // then
        ArgumentCaptor<Status> statusCap = ArgumentCaptor.forClass(Status.class);
        ArgumentCaptor<LocalDateTime> timeCap = ArgumentCaptor.forClass(LocalDateTime.class);

        then(statusRepository).should(times(1))
                .findByStatusName(StatusEnum.ORDER_WAITING);
        then(orderRepository).should(times(1))
                .deleteExpiredWaitingOrders(statusCap.capture(), timeCap.capture());

        assertThat(statusCap.getValue()).isSameAs(waiting);

        LocalDateTime passedExpiredTime = timeCap.getValue();

        assertThat(passedExpiredTime).isAfterOrEqualTo(lowerBound);
        assertThat(passedExpiredTime).isBeforeOrEqualTo(upperBound);

    }

    @Test
    void cleanUpExpiredOrders_삭제수_0이어도_정상호출된다() {
        // given
        Status waiting = new Status(StatusEnum.ORDER_WAITING);
        given(statusRepository.findByStatusName(StatusEnum.ORDER_WAITING)).willReturn(waiting);
        given(orderRepository.deleteExpiredWaitingOrders(any(Status.class), any(LocalDateTime.class)))
                .willReturn(0);

        // when
        scheduler.cleanUpExpiredOrders();

        // then: 호출만 정확히 되었는지 확인
        then(statusRepository).should().findByStatusName(StatusEnum.ORDER_WAITING);
        then(orderRepository).should().deleteExpiredWaitingOrders(eq(waiting), any(LocalDateTime.class));
    }
}