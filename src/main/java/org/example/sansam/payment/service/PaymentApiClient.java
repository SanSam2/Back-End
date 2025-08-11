package org.example.sansam.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.payment.dto.TossPaymentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@Slf4j
public class PaymentApiClient {

    @Value("${toss.confirm-url}")
    private String urlPath;

    @Value("${toss.cancel-url}")
    private String tossCancelUrl;

    @Value("${toss.secret-key}")
    private String secretKey;
    private final RestTemplate restTemplate;


    public PaymentApiClient(RestTemplateBuilder restTemplateBuilder){
        this.restTemplate = restTemplateBuilder.build();
    }


    public Map<String,Object> confirmPayment(TossPaymentRequest paymentRequest){
        // Base64 인코딩
        String encodedAuth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
        String authHeader = "Basic " + encodedAuth;

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authHeader);

        // 요청 바디 설정
        Map<String, Object> payloadMap = Map.of(
                "paymentKey", paymentRequest.getPaymentKey(),
                "orderId", paymentRequest.getOrderId(),
                "amount", paymentRequest.getAmount()
        );
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payloadMap, headers);

        try{
            ResponseEntity<Map> response = restTemplate.postForEntity(urlPath, requestEntity, Map.class);
            if(response.getStatusCode()== HttpStatus.OK){
                return response.getBody();
            }else{
                throw new CustomException(ErrorCode.PAYMENT_FAILED);
            }
        }catch(Exception e){
            throw new CustomException(ErrorCode.API_FAILED);
        }
    }

    public Map<String, Object> tossPaymentCancel(String paymentKey, Long cancelAmount, String cancelReason) {
        String url = tossCancelUrl + "/" + paymentKey + "/cancel";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(secretKey, "");

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("cancelReason", cancelReason);
        requestBody.put("cancelAmount",cancelAmount.toString());

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Map.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new IllegalStateException("결제 취소 요청 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

}
