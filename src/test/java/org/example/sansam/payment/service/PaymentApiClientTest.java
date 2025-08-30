package org.example.sansam.payment.service;

import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.payment.dto.TossPaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;


import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class PaymentApiClientTest {

    private PaymentApiClient client;
    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    @BeforeEach
    void setUp() {
        client = new PaymentApiClient(new RestTemplateBuilder());
        restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        server = MockRestServiceServer.createServer(restTemplate);

        // 테스트용 설정 주입
        ReflectionTestUtils.setField(client, "secretKey", "sk_test_abc");
        ReflectionTestUtils.setField(client, "urlPath", "http://localhost/api/confirm");
        ReflectionTestUtils.setField(client, "tossCancelUrl", "http://localhost/api/payments");
    }


    @Test
    void confirmPayment_성공하면_응답바디를_그대로_리턴한다() {
        //given
        String expectedAuth = "Basic " + Base64.getEncoder()
                .encodeToString(("sk_test_abc:").getBytes(StandardCharsets.UTF_8));

        server.expect(requestTo("http://localhost/api/confirm"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", expectedAuth))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json("""
                  {"paymentKey":"pkey","orderId":"ORD-1","amount":1000}
              """, true))
                .andRespond(withSuccess("{\"ok\":true,\"method\":\"카드\"}", MediaType.APPLICATION_JSON));

        //when
        Map<String, Object> body = client.confirmPayment(
                new TossPaymentRequest("pkey", "ORD-1", 1000L)
        );

        //then
        assertThat(body).containsEntry("ok", true).containsEntry("method", "카드");
        server.verify();
    }

    @Test
    void confirmPayment_200이_아니면_API_FAILED를_던진다() {
        // given
        server.expect(requestTo("http://localhost/api/confirm"))
                .andRespond(withStatus(HttpStatus.CREATED).body("{}").contentType(MediaType.APPLICATION_JSON));

        //when & then
        CustomException ex = assertThrows(CustomException.class, () ->
                client.confirmPayment(new TossPaymentRequest("p", "O", 1L))
        );
        assertEquals(ErrorCode.API_FAILED, ex.getErrorCode()); // else에서 던진 예외도 catch에서 API_FAILED로 래핑됨
        server.verify();
    }

    @Test
    void confirmPayment_서버에러면_API_FAILED를_던진다() {
        //given
        server.expect(requestTo("http://localhost/api/confirm"))
                .andRespond(withServerError());

        //when & then
        CustomException ex = assertThrows(CustomException.class, () ->
                client.confirmPayment(new TossPaymentRequest("p", "O", 1L))
        );
        assertEquals(ErrorCode.API_FAILED, ex.getErrorCode());
        server.verify();
    }

    @Test
    void tossPaymentCancel_성공하면_응답바디를_그대로_리턴한다() {
        //given
        String paymentKey = "pay_123";
        String url = "http://localhost/api/payments/" + paymentKey + "/cancel";

        String expectedAuth = "Basic " + Base64.getEncoder()
                .encodeToString(("sk_test_abc:").getBytes(StandardCharsets.UTF_8));

        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", expectedAuth))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json("""
                  {"cancelReason":"db-failed","cancelAmount":"1000"}
              """, true))
                .andRespond(withSuccess("{\"status\":\"CANCELED\",\"cancelAmount\":1000}", MediaType.APPLICATION_JSON));

        //when
        Map<String, Object> body = client.tossPaymentCancel(paymentKey, 1000L, "db-failed","testIdempotencyKey");

        //then
        assertThat(body).containsEntry("status", "CANCELED").containsEntry("cancelAmount", 1000);
        server.verify();
    }

    @Test
    void tossPaymentCancel_서버에러면_CustomAPIException을_던진다() {
        //given
        String paymentKey = "pay_500";
        String url = "http://localhost/api/payments/" + paymentKey + "/cancel";

        //when
        server.expect(requestTo(url)).andRespond(withServerError());

        CustomException ex = assertThrows(CustomException.class, () ->
                client.tossPaymentCancel(paymentKey, 1L, "x","testIdempotencyKey")
        );

        //then
        assertEquals(ErrorCode.API_INTERNAL_ERROR,ex.getErrorCode());
        server.verify();
    }
}