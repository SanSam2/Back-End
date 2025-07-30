package org.example.sansam.payment.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.order.domain.Order;
import org.example.sansam.payment.dto.TossPaymentRequest;
import org.example.sansam.payment.dto.TossPaymentResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


@Component
@RequiredArgsConstructor
@Slf4j
public class TossPaymentClient {

    private RestTemplate restTemplate;
    private String apiKey="${toss.api.key}";
    private String toss_secret = "test_sk_Gv6LjeKD8aaZW55lQqNw8wYxAdXy";

    public TossPaymentResponse requestPayment(String paymentKey, String orderId, int amount){

        String authorization = Base64.getEncoder().encodeToString(toss_secret.getBytes());
        log.error("------ API 전송 요청 -------" + authorization);

        String url = "https://api.tosspayments.com/v1/payments/confirm";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Basic " + authorization);

        Map<String,Object> requestBody = new HashMap<>();
        requestBody.put("paymentKey", paymentKey);
        requestBody.put("orderId", orderId);
        requestBody.put("amount",amount);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            log.error(entity.getHeaders().toString());
            log.error(entity.getBody().toString());

            ResponseEntity<TossPaymentResponse> response = restTemplate.postForEntity(
                    url,
                    entity,
                    TossPaymentResponse.class
            );

            return response.getBody();
        }catch(HttpClientErrorException e){
            throw new RuntimeException("결제 승인 실패: " + e.getResponseBodyAsString());
        }


    }
}
