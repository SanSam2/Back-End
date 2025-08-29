package org.example.sansam.payment.adapter;

import lombok.RequiredArgsConstructor;
import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CancelResponseNormalize {

    public ParsedCancel parseTossCancelResponse(Map<String, Object> res) {
        if (res == null)
            throw new CustomException(ErrorCode.API_INTERNAL_ERROR);

        String status = String.valueOf(res.get("status"));
        if (!"CANCELED".equalsIgnoreCase(status) && !"PARTIAL_CANCELED".equalsIgnoreCase(status)) {
            throw new CustomException(ErrorCode.PAYMENT_FAILED);
        }

        Object cancelsObj = res.get("cancels");
        if (!(cancelsObj instanceof List<?> cancels) || cancels.isEmpty()) {
            throw new CustomException(ErrorCode.CANCEL_NOT_FOUND);
        }

        Object lastObj = cancels.get(cancels.size() - 1);
        if (!(lastObj instanceof Map<?, ?> last)) {
            throw new CustomException(ErrorCode.RESPONSE_FORM_NOT_RIGHT);
        }

        String cancelReason = String.valueOf(last.get("cancelReason"));
        String canceledAtStr = String.valueOf(last.get("canceledAt"));
        LocalDateTime canceledAt = OffsetDateTime
                .parse(canceledAtStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                .toLocalDateTime();

        Long refundPrice = toLong(last.get("cancelAmount"));
        String paymentKey = String.valueOf(res.get("paymentKey"));

        return new ParsedCancel(paymentKey, refundPrice, cancelReason, canceledAt);
    }

    private long toLong(Object v) {
        if (v instanceof Number n) return n.longValue();

        if (v instanceof String s) {
            if (s.isBlank()) throw new CustomException(ErrorCode.RESPONSE_FORM_NOT_RIGHT);
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                throw new CustomException(ErrorCode.RESPONSE_FORM_NOT_RIGHT);
            }
        }

        throw new CustomException(ErrorCode.RESPONSE_FORM_NOT_RIGHT);
    }

    public record ParsedCancel(
            String paymentKey,
            Long refundPrice,
            String cancelReason,
            LocalDateTime canceledAt
    ) {}
}
