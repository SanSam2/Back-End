package org.example.sansam.notification.template;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.sansam.notification.exception.CustomException;
import org.example.sansam.notification.exception.ErrorCode;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum NotificationType {
    WELCOME(1L, "welcomeMessage"),
    PAYMENT_COMPLETE(2L, "paymentComplete"),
    PAYMENT_CANCEL(3L, "paymentCancel"),
    CART_LOW(4L, "cartProductStockLowMessage"),
    WISH_LOW(5L, "wishListProductStockLow"),
    REVIEW_REQUEST(6L, "reviewRequestMessage"),
    CHAT(7L, "chatNotificationMessage");

    private final Long templateId;
    private final String eventName;

    public static NotificationType getNotificationType(Long templateId) {
        return Arrays.stream(values())
                .filter(notificationType -> notificationType.getTemplateId().equals(templateId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND));
    }
}
