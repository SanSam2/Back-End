package org.example.sansam.order.domain;

import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.order.domain.nameformatter.KoreanOrdernameFormatter;
import org.example.sansam.order.domain.nameformatter.OrderNameFormatter;
import org.example.sansam.order.domain.ordernumber.OrderNumberPolicy;
import org.example.sansam.order.domain.pricing.PricingPolicy;
import org.example.sansam.product.domain.Product;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.example.sansam.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class OrderTest {

    private Status orderWaitingStatus;
    private User testUser;

    private User stubUser(long id) {
        User u = mock(User.class);
        given(u.getId()).willReturn(id);
        return u;
    }

    private static class fakePolicy implements PricingPolicy {
        List<OrderProduct> captured;
        long toReturn;

        fakePolicy(long toReturn) {
            this.toReturn = toReturn;
        }

        @Override
        public Long totalOf(List<OrderProduct> products) {
            this.captured = products;
            return toReturn;
        }
    }

    private static class fakeOrderNumberPolicy implements OrderNumberPolicy {

        @Override
        public String makeOrderNumber() {
            return "1234567890-123e4567-e89b-12d3-a456-426614174000";
        }
    }

    private static void setPrivateList(Object target, String fieldName, List<?> value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static void set(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    @BeforeEach
    void setUp() {
        orderWaitingStatus = new Status(StatusEnum.ORDER_WAITING);
        testUser = stubUser(1L);
    }


    private OrderProduct temp(String productName, long unitPrice, int qty) {
        var product = mock(org.example.sansam.product.domain.Product.class);
        given(product.getProductName()).willReturn(productName);

        return OrderProduct.create(product, unitPrice, qty, "M", "BLACK", "url", orderWaitingStatus);
    }
}