package org.example.sansam.exception.pay;

public enum ErrorCode {
    ORDER_NOT_FOUND("주문을 찾을 수 없습니다."),
    PAYMENT_FAILED("결제에 실패했습니다."),
    INVALID_REQUEST("잘못된 요청입니다."),
    API_FAILED("Toss 결제 승인 API 통신 실패"),
    UNSUPPORTED_PAYMENT_METHOD("지원하지 않는 결제 수단입니다."),
    PAYMENT_CONFIRM_FAILED("결제 승인 중 오류가 발생했습니다."),
    NOT_ENOUGH_STOCK("주문하신 상품 중 품절된 상품이 포함되었습니다."),
    PRODUCT_NOT_FOUND("상품을 찾을 수 없습니다."),
    ORDER_ALREADY_FINISHED("이미 처리된 주문입니다."),
    NOT_EQUAL_COST("결제금액이 주문금액과 일치하지 않습니다."),
    NO_ITEM_IN_ORDER("주문에 상품이 없습니다."),
    INTERNAL_SERVER_ERROR("내부 서버 오류입니다."),
    API_INTERNAL_ERROR("API 내부 오류 발생입니다."),
    CANCEL_NOT_FOUND("취소 내역이 존재하지 않습니다."),
    RESPONSE_FORM_NOT_RIGHT("응답 형식이 옳지 않습니다.");

    private final String message;

    ErrorCode(String message){
        this.message=message;
    }

    public String getMessage(){
        return message;
    }
}
