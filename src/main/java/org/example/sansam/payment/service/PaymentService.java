package org.example.sansam.payment.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.service.OrderService;
import org.example.sansam.payment.domain.PaymentMethodType;
import org.example.sansam.payment.domain.Payments;
import org.example.sansam.payment.domain.PaymentsType;
import org.example.sansam.payment.dto.TossPaymentRequest;
import org.example.sansam.payment.repository.PaymentsRepository;
import org.example.sansam.payment.repository.PaymentsTypeRepository;
import org.example.sansam.user.service.UserService;

import org.springframework.stereotype.Service;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final UserService userService;
    private final OrderService orderService;

    private final PaymentsTypeRepository paymentsTypeRepository;
    private final PaymentsRepository paymentsRepository;

    private String SECRET_KEY = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";

    public void confirmPayment(TossPaymentRequest request) throws Exception {
        String paymentKey = request.getPaymentKey();
        String orderId = request.getOrderId();
        Long amount = request.getAmount();
        log.error(orderId);
        log.error(paymentKey);
        log.error(amount.toString());

        String basicAuth = "Basic " + java.util.Base64.getEncoder().encodeToString((SECRET_KEY + ":").getBytes());
        log.error(basicAuth);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tosspayments.com/v1/payments/confirm"))
                .header("Authorization", basicAuth)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(String.format(
                        "{\"paymentKey\":\"%s\",\"orderId\":\"%s\",\"amount\":%d}",
                        paymentKey, orderId, amount)))
                .build();

        log.error(httpRequest.uri().toString());
        log.error(httpRequest.headers().toString());


        HttpResponse<String> response = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
        log.error(response.body());
        log.error(response.headers().toString());
        if (response.statusCode() == 200) {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response.body());
            Order order =orderService.findById(Long.valueOf(request.getOrderId())).orElse(null);


            log.error(order.toString());

            String methodKor = jsonNode.get("method").asText();
            PaymentMethodType type = PaymentMethodType.fromKorean(methodKor);
            PaymentsType paymentsType = paymentsTypeRepository.findByTypeName(type)
                    .orElseThrow(() -> new IllegalArgumentException("결제 수단이 존재하지 않습니다"));
            log.error(paymentsType.toString());

            Payments payment = Payments.builder()
                    .order(order)
                    .paymnetsType(paymentsType)
                    .totalPrice(jsonNode.get("totalAmount").asLong())
                    .finalPrice(jsonNode.get("approvedAmount").asLong())
                    .createdAt(LocalDateTime.now())
                    .build();

            paymentsRepository.save(payment);
        } else {
            log.error("결제 승인 실패: {}", response.body());
            throw new RuntimeException("결제 승인 실패: " + response.body());
        }
    }


}
