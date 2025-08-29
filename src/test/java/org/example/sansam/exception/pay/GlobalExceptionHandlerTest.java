package org.example.sansam.exception.pay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;


class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    static Stream<Arguments> mappingCases() {
        return Stream.of(
                // 400
                args(ErrorCode.INVALID_REQUEST, HttpStatus.BAD_REQUEST),
                args(ErrorCode.RESPONSE_FORM_NOT_RIGHT, HttpStatus.BAD_REQUEST),
                args(ErrorCode.CHECK_STATUS, HttpStatus.BAD_REQUEST),
                args(ErrorCode.NO_ITEM_IN_ORDER, HttpStatus.BAD_REQUEST),
                args(ErrorCode.PAYMENT_FAILED, HttpStatus.BAD_REQUEST),
                args(ErrorCode.PAYMENT_CONFIRM_FAILED, HttpStatus.BAD_REQUEST),
                args(ErrorCode.UNSUPPORTED_PAYMENT_METHOD, HttpStatus.BAD_REQUEST),

                // 404
                args(ErrorCode.ORDER_NOT_FOUND, HttpStatus.NOT_FOUND),
                args(ErrorCode.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND),
                args(ErrorCode.CANCEL_NOT_FOUND, HttpStatus.NOT_FOUND),
                args(ErrorCode.NO_USER_ERROR, HttpStatus.NOT_FOUND),
                args(ErrorCode.CANNOT_FIND_FILE_IMAGE, HttpStatus.NOT_FOUND),

                // 409
                args(ErrorCode.NOT_ENOUGH_STOCK, HttpStatus.CONFLICT),
                args(ErrorCode.NO_ITEM, HttpStatus.CONFLICT),
                args(ErrorCode.ZERO_STOCK, HttpStatus.CONFLICT),
                args(ErrorCode.NOT_EQUAL_COST, HttpStatus.CONFLICT),
                args(ErrorCode.PRICE_TAMPERING, HttpStatus.CONFLICT),
                args(ErrorCode.ORDER_ALREADY_FINISHED, HttpStatus.CONFLICT),

                // 422
                args(ErrorCode.CANCEL_QUANTITY_MUST_MORE_THEN_ZERO, HttpStatus.UNPROCESSABLE_ENTITY),
                args(ErrorCode.CANNOT_CANCEL_MORE_THAN_ORDERED_QUANTITY, HttpStatus.UNPROCESSABLE_ENTITY),

                // 502
                args(ErrorCode.API_FAILED, HttpStatus.BAD_GATEWAY),
                args(ErrorCode.API_INTERNAL_ERROR, HttpStatus.BAD_GATEWAY),

                // 500
                args(ErrorCode.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR)
        );
    }

    private static Arguments args(ErrorCode c, HttpStatus s) {
        return Arguments.of(c, s);
    }


    @ParameterizedTest
    @CsvSource({
            "PAYMENT_FAILED, 결제 게이트웨이 일시 오류",
            "API_FAILED, 결제 서버 타임아웃"
    })
    void handleCustom_사용시_메시지가_잘_나온다(String codeName, String expected) {
        ErrorCode code = ErrorCode.valueOf(codeName);
        CustomException ex = new CustomException(code,expected);

        ResponseEntity<GlobalExceptionHandler.ApiError> response = handler.handleCustom(ex);

        assertThat(response.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(code.name());
        assertThat(response.getBody().message()).isEqualTo(expected);

    }

    @ParameterizedTest
    @CsvSource({"API_INTERNAL_ERROR", "API_FAILED"})
    void cause는_그대로_보존된다(String codeName) {
        //given
        ErrorCode code = ErrorCode.valueOf(codeName);
        Throwable cause = new RuntimeException("timeout-occurred");

        //when
        CustomException ex = new CustomException(code, cause);

        //then
        assertThat(ex.getCause()).isSameAs(cause);
        assertThat(ex).hasMessage(code.getMessage());
        assertThat(ex.getCause()).hasMessage("timeout-occurred");
    }
}