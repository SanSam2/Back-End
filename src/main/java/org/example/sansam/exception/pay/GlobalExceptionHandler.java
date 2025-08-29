package org.example.sansam.exception.pay;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public static record ApiError(String code, String message){
        public static ApiError of(String code, String message){
            return new ApiError(code, message);
        }
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiError> handleCustom (CustomException e) {
        HttpStatus status = toStatus(e.getErrorCode());
        return ResponseEntity.status(status)
                .body(ApiError.of(e.getErrorCode().name(), e.getMessage()));
    }


    private HttpStatus toStatus(ErrorCode code) {
        return switch (code) {
            // 400
            case INVALID_REQUEST, RESPONSE_FORM_NOT_RIGHT, CHECK_STATUS, NO_ITEM_IN_ORDER, ORDER_NOT_CANCELABLE,ORDER_PRODUCT_NOT_BELONGS_TO_ORDER
                    -> HttpStatus.BAD_REQUEST;

            // 404
            case ORDER_NOT_FOUND, PRODUCT_NOT_FOUND, CANCEL_NOT_FOUND, NO_USER_ERROR, CANNOT_FIND_FILE_IMAGE, PAYMENTS_NOT_FOUND
                    -> HttpStatus.NOT_FOUND;

            // 409
            case NOT_ENOUGH_STOCK, NO_ITEM, ZERO_STOCK, NOT_EQUAL_COST, PRICE_TAMPERING, ORDER_ALREADY_FINISHED
                    -> HttpStatus.CONFLICT;

            // 결제/수단
            case PAYMENT_FAILED, PAYMENT_CONFIRM_FAILED, UNSUPPORTED_PAYMENT_METHOD
                    -> HttpStatus.BAD_REQUEST;

            // 외부 API
            case API_FAILED, API_INTERNAL_ERROR
                    -> HttpStatus.BAD_GATEWAY;

            // 422
            case CANCEL_QUANTITY_MUST_MORE_THEN_ZERO, CANNOT_CANCEL_MORE_THAN_ORDERED_QUANTITY,PAYMENT_REQUIRE_ABSCENT,
                 ORDER_AND_PAY_NOT_EQUAL
                    -> HttpStatus.UNPROCESSABLE_ENTITY;

            // 500
            case INTERNAL_SERVER_ERROR,PAYMENTS_TYPE_NOT_CONFIGURED,API_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
