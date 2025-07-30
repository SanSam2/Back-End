package org.example.sansam.payment.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.dto.OrderRequest;
import org.example.sansam.order.dto.OrderResponse;
import org.example.sansam.order.dto.PaymentRequestEvent;
import org.example.sansam.payment.dto.TossPaymentRequest;
import org.example.sansam.payment.dto.TossPaymentResponse;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final UserService userService;
    private String toss_secret = "test_sk_Gv6LjeKD8aaZW55lQqNw8wYxAdXy";
    private String url = "https://api.tosspayments.com/v2/payments";


    public TossPaymentResponse requestPayment(TossPaymentRequest paymentRequest, Long userId) {

        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저 없음"));

        String authorization = Base64.getEncoder().encodeToString(toss_secret.getBytes());


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Basic " + authorization);

        Map<String,Object> requestBody = new HashMap<>();
        requestBody.put("method","CARD");
        requestBody.put("amount",paymentRequest.getAmount());
        requestBody.put("orderId", paymentRequest.getOrderId());
        requestBody.put("orderName", paymentRequest.getOrderName());
        requestBody.put("userName", user.getName());
        requestBody.put("successUrl",paymentRequest.getSuccessUrl());
        requestBody.put("failUrl",paymentRequest.getFailUrl());

        HttpEntity<Map<String, Object>> requestData = new HttpEntity<>(requestBody, headers);

        ResponseEntity<TossPaymentResponse> response;
        try {
            RestTemplate restTemplate = new RestTemplate();

            response = restTemplate.postForEntity(
                    url,
                    requestData,
                    TossPaymentResponse.class
            );

        }catch(HttpClientErrorException e){
            throw new RuntimeException("토스페이먼츠 응답 : "+ e.getMessage()+" ");
        }
        return response.getBody();

    }


}
