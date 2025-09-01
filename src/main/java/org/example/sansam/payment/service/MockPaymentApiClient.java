package org.example.sansam.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.example.sansam.payment.dto.TossPaymentRequest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@Primary
@Profile("perf")
public class MockPaymentApiClient extends PaymentApiClient{

    public MockPaymentApiClient() {
        super(new RestTemplateBuilder());
    }

    @Override
    public Map<String, Object> confirmPayment(TossPaymentRequest req) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {

        }
        ZoneId kst = ZoneId.of("Asia/Seoul");
        String now = ZonedDateTime.now(kst).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        Map<String, Object> card = new HashMap<>();
        card.put("issuerCode", "71");
        card.put("acquirerCode", "71");
        card.put("number", "12345678****000*");
        card.put("installmentPlanMonths", 0);
        card.put("isInterestFree", false);
        card.put("interestPayer", null);
        card.put("approveNo", "00000000");
        card.put("useCardPoint", false);
        card.put("cardType", "신용");
        card.put("ownerType", "개인");
        card.put("acquireStatus", "READY");
        card.put("amount", req.getAmount());

        Map<String, Object> easyPay = Map.of("provider","토스페이", "amount",0, "discountAmount",0);

        Map<String, Object> receipt = Map.of(
                "url", "https://dashboard.tosspayments.com/receipt/redirection?transactionId=mock&ref=PX"
        );
        Map<String, Object> checkout = Map.of(
                "url", "https://api.tosspayments.com/v1/payments/" + req.getPaymentKey() + "/checkout"
        );

        Map<String, Object> body = new HashMap<>();
        body.put("mId", "tosspayments");
        body.put("lastTransactionKey", UUID.randomUUID().toString().replace("-", "").toUpperCase());
        body.put("paymentKey", req.getPaymentKey());
        body.put("orderId", req.getOrderId());         // orderNumber
        body.put("orderName", "mock-order");
        body.put("taxExemptionAmount", 0);
        body.put("status", "DONE");
        body.put("requestedAt", now);
        body.put("approvedAt", now);
        body.put("useEscrow", false);
        body.put("cultureExpense", false);
        body.put("card", card);
        body.put("virtualAccount", null);
        body.put("transfer", null);
        body.put("mobilePhone", null);
        body.put("giftCertificate", null);
        body.put("cashReceipt", null);
        body.put("cashReceipts", null);
        body.put("discount", null);
        body.put("cancels", null);
        body.put("secret", null);
        body.put("type", "NORMAL");
        body.put("easyPay", easyPay);
        body.put("country", "KR");
        body.put("failure", null);
        body.put("isPartialCancelable", true);
        body.put("receipt", receipt);
        body.put("checkout", checkout);
        body.put("currency", "KRW");
        body.put("totalAmount", req.getAmount());
        body.put("balanceAmount", req.getAmount());
        body.put("suppliedAmount", Math.round(req.getAmount() * 0.909));
        body.put("vat", Math.round(req.getAmount() * 0.091));
        body.put("taxFreeAmount", 0);
        body.put("metadata", null);
        body.put("method", "카드");
        body.put("version", "2022-11-16");

        log.info("[MOCK] confirm payment (1s delay): orderId={}, amount={}", req.getOrderId(), req.getAmount());
        return body;
    }

    @Override
    public Map<String, Object> tossPaymentCancel(String paymentKey, Long cancelAmount, String reason, String idempotencyKey) {
        // 필요 시 여기에도 sleep 추가 가능
        Map<String, Object> body = new HashMap<>();
        body.put("status", "CANCELED");
        body.put("paymentKey", paymentKey);
        body.put("canceledAmount", cancelAmount);
        body.put("idempotencyKey", idempotencyKey);
        return body;
    }
}
