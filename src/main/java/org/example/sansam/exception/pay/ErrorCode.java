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
    RESPONSE_FORM_NOT_RIGHT("응답 형식이 옳지 않습니다."),
    NO_ITEM("구매하려는 상품의 재고가 없습니다."),
    PRICE_TAMPERING("가격이 일치하지 않습니다."),
    ZERO_STOCK("재고가 0입니다."),
    CANCEL_QUANTITY_MUST_MORE_THEN_ZERO("취소 수량은 0보다 커야합니다."),
    CANNOT_CANCEL_MORE_THAN_ORDERED_QUANTITY("취소 수량은 주문 수량보다 작거나 같습니다."),
    CHECK_STATUS("상태값을 서버에서 확인해주세요"),
    NO_USER_ERROR("해당 이메일을 가진 사용자를 찾을 수 없습니다."),
    CANNOT_FIND_FILE_IMAGE("이미지를 찾을 수 없습니다."),
    PAYMENT_REQUIRE_ABSCENT("Payment 필수값 누락되었습니다."),
    ORDER_AND_PAY_NOT_EQUAL("주문정보와 결제 금액이 같지 않습니다."),
    PAYMENTS_TYPE_NOT_CONFIGURED("결제 수단이 서버에서 확인되지 않습니다."),
    API_SERVER_ERROR("토스 API 서버에 이상이 발생했습니다."),
    ORDER_NOT_CANCELABLE("취소하지 못하는 주문입니다."),
    ORDER_PRODUCT_NOT_BELONGS_TO_ORDER("주문과 주문상품이 매칭되지 않습니다."),
    PAYMENTS_NOT_FOUND("결제 정보를 찾을 수 없습니다."),
    OUTBOX_PAYLOAD_PARSE_ERROR("메시지큐 파싱 에러");

    private final String message;

    ErrorCode(String message){
        this.message=message;
    }

    public String getMessage(){
        return message;
    }
}
