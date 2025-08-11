package org.example.sansam.payment.domain;

import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;

public enum PaymentMethodType {
    CARD, EASY_PAY, TRANSFER, VIRTUAL_ACCOUNT;

    public static PaymentMethodType fromKorean(String kor) {
        return switch (kor) {
            case "카드" -> CARD;
            case "간편결제" -> EASY_PAY;
            case "계좌이체" -> TRANSFER;
            case "가상계좌" -> VIRTUAL_ACCOUNT;
            default -> throw new CustomException(ErrorCode.UNSUPPORTED_PAYMENT_METHOD);
        };
    }
}

