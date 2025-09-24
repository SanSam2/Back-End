# 🕒 SanSam's Ecumus Platform

이 프로젝트는 **명품 판매 이커머스 플랫폼**입니다.  
사용자는 명품들만 모여있는 쇼핑몰을 온라인으로 경험할 수 있습니다.
초고가 상품으로 명품들로만 모여있는 쇼핑몰을 경험시켜 드립니다.
일반적인 상품 구입, 리뷰 작성, 위시리스트 등록, 실시간 채팅 등 
다양한 커머스 기능을 제공합니다.

---
## 팀소개
<img width="669" height="319" alt="image" src="https://github.com/user-attachments/assets/d7cd5382-a968-4be7-b7a8-7c32c5284e38" />

---
## Infra
<img width="723" height="427" alt="image" src="https://github.com/user-attachments/assets/70c18b4f-ee3a-43bc-a47b-175a7f2a5cc1" />

---
## ERD
<img width="4320" height="2382" alt="HotDealPlatform (7)" src="https://github.com/user-attachments/assets/408df384-e3d3-401a-8013-834373535544" />

---
## 🚀 주요 기능

### ✅ 사용자 (User)
- 회원가입
- 마이페이지 정보 수정 및 탈퇴
- 세션 기반 로그인

---
## 은경

### ✅ 상품 (Product)
- 상품 등록/조회/삭제 (관리자)
- 검색, 정렬, 카테고리 조회
- 위시리스트 등록/해제

### ✅ 장바구니 (Cart)
- 상품 담기 / 삭제 / 목록 조회

### ✅ 리뷰 (Review)
- 구매 상품에 대한 리뷰 작성/수정/삭제
- 상품 상세 페이지에서 리뷰 전체 조회

---
## 슬빈

### ✅ 주문 및 결제 (Order / Payment)
- 주문서 저장 / 삭제
- 토스 API 연동 결제/결제 취소

### ✅ 재고 (Stock)
- 재고 차감
- 재고 증가

### ✅ 상태 (Status)
- DB에서 이미 저장되어있는 상태 찾기

### 프로젝트 여정기

[👨🏻‍💻 1. 재고 ERD 분리 과정](https://github.com/SanSam2/Back-End/wiki/%EC%9E%AC%EA%B3%A0-ERD-%EB%B6%84%EB%A6%AC-%EA%B3%BC%EC%A0%95)

[👨🏻‍💻 2. 테스트코드 작성과 보상 OutBox 구현기](https://github.com/SanSam2/Back-End/wiki/Jacoco-%ED%85%8C%EC%8A%A4%ED%8A%B8%EC%BB%A4%EB%B2%84%EB%A6%AC%EC%A7%80-100%25-%EB%8B%AC%EC%84%B1%EA%B8%B0-%28feat.-DLQ%EB%A5%BC-RDB%EB%A1%9C-%EA%B5%AC%ED%98%84%ED%95%B4%EB%82%B4%EA%B8%B0%29)

[👨🏻‍💻 3. Order 트랜잭션 분리 & DB Indexing을 통한 최적화](https://github.com/SanSam2/Back-End/wiki/Order-%ED%8A%B8%EB%9E%9C%EC%9E%AD%EC%85%98-%EB%B6%84%EB%A6%AC-%26-DB-Indexing%EC%9D%84-%ED%86%B5%ED%95%9C-%EC%B5%9C%EC%A0%81%ED%99%94)

[👨🏻‍💻 4. HikariCP 튜닝 및 테스트 툴 변경](https://github.com/SanSam2/Back-End/wiki/HikariCP-%ED%8A%9C%EB%8B%9D-%EB%B0%8F-%ED%85%8C%EC%8A%A4%ED%8A%B8-%ED%88%B4-%EB%B3%80%EA%B2%BD)

[👨🏻‍💻 5. 상태값 캐싱 & 재고 이벤트 비동기 처리(단일 서버)](https://github.com/SanSam2/Back-End/wiki/%EC%83%81%ED%83%9C%EA%B0%92-%EC%BA%90%EC%8B%B1-%26-%EC%9E%AC%EA%B3%A0-%EC%9D%B4%EB%B2%A4%ED%8A%B8-%EB%B9%84%EB%8F%99%EA%B8%B0-%EC%B2%98%EB%A6%AC%28%EB%8B%A8%EC%9D%BC-%EC%84%9C%EB%B2%84%29)

[👨🏻‍💻 6. 재고 서버 분리 with RabbitMQ](https://github.com/SanSam2/Back-End/wiki/Stock-%EC%84%9C%EB%B2%84-%EB%B6%84%EB%A6%AC-%EC%97%AC%EC%A0%95%EA%B8%B0-%28SanSam-%EC%A3%BC%EB%AC%B8---%EA%B2%B0%EC%A0%9C---%EC%83%81%ED%83%9C-MSA-%EC%97%AC%EC%A0%95%EA%B8%B0%29--%281%29)

---
## 호상

### ✅ 알림 시스템 (Notification)
- 결제 완료/취소 이메일 및 홈페이지 알림
- 리뷰 요청 / 재고 부족 알림

---
## 한섭

### ✅ 채팅 (Chat)
- 조건 기반 오픈 채팅방 생성/입장/퇴장
- 실시간 메시지 전송 및 수신
- 채팅 읽음 처리 및 배지 알림
