package org.example.sansam.payment.adapter;

import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class CancelResponseNormalizeTest {

    CancelResponseNormalize normalize = new CancelResponseNormalize();

    @Test
    void 성공한_Canceled에_대해서_파싱이_성공한다() {
        //given
        Map<String, Object> response = new HashMap<>();
        response.put("status", "CANCELED");
        response.put("paymentKey", "pk_123");
        response.put("cancels", List.of(Map.of(
                "cancelReason", "테스트 결제 취소",
                "canceledAt", "2024-02-13T12:20:23+09:00",
                "cancelAmount", 1000 // Number
        )));

        //when
        var parsed = normalize.parseTossCancelResponse(response);

        //then
        assertThat(parsed.paymentKey()).isEqualTo("pk_123");
        assertThat(parsed.refundPrice()).isEqualTo(1000L);
        assertThat(parsed.cancelReason()).isEqualTo("테스트 결제 취소");
        assertThat(parsed.canceledAt()).isEqualTo(LocalDateTime.of(2024, 2, 13, 12, 20, 23));
    }

    @Test
    void 부분취소에_대해_파싱된다() {
        //given
        Map<String, Object> res = new HashMap<>();
        res.put("status", "PARTIAL_CANCELED");
        res.put("paymentKey", "pk_abc");
        res.put("cancels", List.of(Map.of(
                "cancelReason", "부분취소",
                "canceledAt", "2024-02-13T12:20:23+09:00",
                "cancelAmount", "1500"
        )));

        //when
        var parsed = normalize.parseTossCancelResponse(res);

        //then
        assertThat(parsed.paymentKey()).isEqualTo("pk_abc");
        assertThat(parsed.refundPrice()).isEqualTo(1500L);
        assertThat(parsed.cancelReason()).isEqualTo("부분취소");
        assertThat(parsed.canceledAt()).isEqualTo(LocalDateTime.of(2024, 2, 13, 12, 20, 23));
    }

    @Test
    void 응답이_null값이면_error가_터진다() {
        //given & when & then
        assertThatThrownBy(() -> normalize.parseTossCancelResponse(null))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.API_INTERNAL_ERROR);
    }

    @Test
    void 상태가_canceled가_아니면_에러가_터진다() {
        //given
        Map<String, Object> res = Map.of("status", "APPROVED");

        //when & then
        assertThatThrownBy(() -> normalize.parseTossCancelResponse(res))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_FAILED);
    }

    @Test
    void 응답값에_cancels가없거나_리스트가_아니면_에러가_난다() {
        //given
        Map<String, Object> res = new HashMap<>();
        res.put("status", "CANCELED");
        assertThatThrownBy(() -> normalize.parseTossCancelResponse(res))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CANCEL_NOT_FOUND);

        //when & then
        res.put("cancels", "oops");
        assertThatThrownBy(() -> normalize.parseTossCancelResponse(res))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CANCEL_NOT_FOUND);
    }

    @Test
    void 응답값안에_cancels가_비어있으면_에러가_난다() {
        //given
        Map<String, Object> res = new HashMap<>();
        res.put("status", "CANCELED");
        res.put("paymentKey", "pk");
        res.put("cancels", List.of()); // empty

        //when & then
        assertThatThrownBy(() -> normalize.parseTossCancelResponse(res))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CANCEL_NOT_FOUND);
    }

    @Test
    void 마지막_취소가() {
        //given
        Map<String, Object> res = new HashMap<>();
        res.put("status", "CANCELED");
        res.put("paymentKey", "pk");
        res.put("cancels", List.of("not-a-map"));

        //when & then
        assertThatThrownBy(() -> normalize.parseTossCancelResponse(res))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESPONSE_FORM_NOT_RIGHT);
    }

    @Test
    void cancelAmount가_없거나_잘못된_자료형일_경우_문제가_된다() {
        //given
        Map<String, Object> last = new HashMap<>();
        last.put("cancelReason", "사유");
        last.put("canceledAt", "2024-02-13T12:20:23+09:00");

        Map<String, Object> res = new HashMap<>();
        res.put("status", "CANCELED");
        res.put("paymentKey", "pk");
        res.put("cancels", List.of(last));

        //when & then
        assertThatThrownBy(() -> normalize.parseTossCancelResponse(res))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESPONSE_FORM_NOT_RIGHT);

        // 잘못된 타입(파싱 불가 문자열)
        last.put("cancelAmount", "not-a-number");
        assertThatThrownBy(() -> normalize.parseTossCancelResponse(res))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESPONSE_FORM_NOT_RIGHT);
    }


    @Test
    void canceldAt자체가_잘못된_형태이면_에러가_발생한다() {
        //given
        Map<String, Object> res = new HashMap<>();
        res.put("status", "CANCELED");
        res.put("paymentKey", "pk");
        res.put("cancels", List.of(Map.of(
                "cancelReason", "사유",
                "canceledAt", "2024-02-13 12:20:23", // ISO_OFFSET_DATE_TIME 아님
                "cancelAmount", 1000
        )));

        //when & then
        assertThatThrownBy(() -> normalize.parseTossCancelResponse(res))
                .isInstanceOf(DateTimeParseException.class); // 현재 구현은 그대로 전파함
    }

    @Test
    void cancelReason이_없어지면_문제가_발생한다() {
        //given
        Map<String, Object> last = new HashMap<>();
        last.put("canceledAt", "2024-02-13T12:20:23+09:00");
        last.put("cancelAmount", 1000);

        Map<String, Object> res = new HashMap<>();
        res.put("status", "CANCELED");
        res.put("paymentKey", "pk");
        res.put("cancels", List.of(last));

        //when
        var parsed = normalize.parseTossCancelResponse(res);

        //then
        assertThat(parsed.cancelReason()).isEqualTo("null");
    }

    @Test
    void cancelAmount가_빈문자열_혹은_공백이면_RESPONSE_FORM_NOT_RIGHT() {
        Map<String, Object> base = new HashMap<>();
        base.put("status", "CANCELED");
        base.put("paymentKey", "pk");

        //빈문자열 태우기
        Map<String, Object> last1 = new HashMap<>();
        last1.put("cancelReason", "사유");
        last1.put("canceledAt", "2024-02-13T12:20:23+09:00");
        last1.put("cancelAmount", "");
        Map<String, Object> res1 = new HashMap<>(base);
        res1.put("cancels", List.of(last1));

        assertThatThrownBy(() -> normalize.parseTossCancelResponse(res1))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESPONSE_FORM_NOT_RIGHT);

        // 공백 태우기
        Map<String, Object> last2 = new HashMap<>(last1);
        last2.put("cancelAmount", "   ");
        Map<String, Object> res2 = new HashMap<>(base);
        res2.put("cancels", List.of(last2));

        assertThatThrownBy(() -> normalize.parseTossCancelResponse(res2))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESPONSE_FORM_NOT_RIGHT);
    }

}