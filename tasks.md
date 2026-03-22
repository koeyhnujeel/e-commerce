# Tasks

## 공통 선행 작업

- [ ] `spec.md`와 `plan.md` 기준 용어를 최종 확정한다.
- [ ] `catalog`, `order`, `payment`, `like`, `member` 모듈 간 책임을 다시 확인한다.
- [ ] 주문 상태 모델 초안을 정리한다.
- [ ] 결제 상태 모델 초안을 정리한다.
- [ ] 신규 `ErrorCode` 후보를 정리한다.
- [ ] `SecurityConfiguration`에 추가될 인증 엔드포인트 목록을 정리한다.
- [ ] `payment` 모듈 패키지 구조를 생성한다.
- [ ] 토스페이먼츠 환경변수 키 목록을 애플리케이션 설정에 반영할 준비를 한다.

## Phase 1. 상품 조회 기반 만들기

### catalog domain

- [x] `Product`에 판매 상태 필드 또는 판매 가능 여부 판단 메서드를 추가한다.
- [ ] `Product`에 생성일시/수정일시가 필요한지 결정한다.
- [x] 대표 이미지 선택 로직의 위치를 정한다.
- [x] 옵션의 주문 가능 여부 판단 기준을 정한다.

### catalog application

- [x] 상품 목록 조회 DTO를 정의한다.
- [x] 상품 상세 조회 DTO를 정의한다.
- [x] 상품 목록 정렬 enum을 정의한다.
- [x] 상품 목록 조회 서비스 유스케이스를 추가한다.
- [x] 상품 상세 조회 서비스 유스케이스를 추가한다.

### catalog persistence

- [x] 상품 목록 조회용 repository 쿼리를 추가한다.
- [x] 카테고리 필터 조건을 지원한다.
- [x] 가격 정렬 조건을 지원한다.
- [x] 최신순 정렬 조건을 지원한다.
- [x] 상품 상세 조회 시 이미지/옵션 fetch 전략을 조정한다.

### catalog adapter/in

- [x] `GET /api/products` 컨트롤러를 추가한다.
- [x] `GET /api/products/{productId}` 컨트롤러를 추가한다.
- [ ] 페이지네이션 쿼리 파라미터를 요청 객체로 정리할지 결정한다.

### tests

- [x] 상품 판매 상태 도메인 테스트를 추가한다.
- [x] 상품 목록 서비스 테스트를 추가한다.
- [x] 상품 상세 서비스 테스트를 추가한다.
- [x] 상품 목록 API 통합 테스트를 추가한다.
- [x] 상품 상세 API 통합 테스트를 추가한다.

## Phase 2. 좋아요 읽기 연동

### like module

- [x] 상품별 좋아요 수 조회 포트를 추가한다.
- [x] 회원 기준 상품 좋아요 여부 조회 포트를 추가한다.
- [x] 상품 ID 목록 기준 벌크 좋아요 집계 조회를 추가한다.
- [x] `LikeRepository` 또는 adapter 쿼리를 확장한다.

### catalog integration

- [x] 상품 목록 응답에 좋아요 수를 포함한다.
- [x] 상품 상세 응답에 좋아요 수를 포함한다.
- [x] 상품 목록 응답에 `likedByMe`를 포함한다.
- [x] 상품 상세 응답에 `likedByMe`를 포함한다.
- [x] 비로그인 사용자의 `likedByMe = false` 처리 로직을 추가한다.

### tests

- [x] 좋아요 집계 조회 repository 테스트를 추가한다.
- [x] 상품 목록 응답 조합 서비스 테스트를 추가한다.
- [x] 상품 상세 응답 조합 서비스 테스트를 추가한다.
- [x] 로그인 사용자 상품 목록 API 테스트를 추가한다.
- [x] 비로그인 사용자 상품 목록 API 테스트를 추가한다.
- [x] 로그인 사용자 상품 상세 API 테스트를 추가한다.

## Phase 3. 주문 생성, 목록, 상세

### order domain

- [x] `Order`에 `orderNumber` 필드를 추가한다.
- [x] `Order`에 `totalAmount` 필드를 추가한다.
- [x] `Order` 생성 규칙을 재설계한다.
- [x] `OrderItem`에 상품명 스냅샷 필드를 추가한다.
- [x] `OrderItem`에 옵션 색상/사이즈 스냅샷 필드를 추가한다.
- [x] `OrderItem`에 주문 당시 가격 스냅샷 정책을 정리한다.
- [x] 주문 총액 계산 책임을 도메인에 추가한다.

### order application

- [x] 주문 생성 command/DTO를 정의한다.
- [x] 주문 목록 조회 DTO를 정의한다.
- [x] 주문 상세 조회 DTO를 정의한다.
- [x] 주문 생성 서비스 유스케이스를 추가한다.
- [x] 내 주문 목록 조회 서비스 유스케이스를 추가한다.
- [x] 내 주문 상세 조회 서비스 유스케이스를 추가한다.
- [x] 타인 주문 접근 차단 로직을 추가한다.

### cross-module integration

- [x] `catalog`에 주문 생성용 상품/옵션 검증 API를 보강한다.
- [x] 주문 생성 시 상품 존재 여부를 검증한다.
- [x] 주문 생성 시 옵션 존재 여부를 검증한다.
- [x] 주문 생성 시 서버 계산 금액만 사용하도록 처리한다.

### order persistence

- [x] 주문 저장 구조 변경에 맞게 JPA 매핑을 수정한다.
- [x] 내 주문 목록 조회 쿼리를 추가한다.
- [x] 내 주문 상세 조회 쿼리를 추가한다.
- [x] 주문 번호 유니크 제약을 추가한다.

### order adapter/in

- [x] `POST /api/orders` 컨트롤러를 추가한다.
- [x] `GET /api/orders` 컨트롤러를 추가한다.
- [x] `GET /api/orders/{orderId}` 컨트롤러를 추가한다.
- [x] 주문 생성/조회 엔드포인트를 인증 대상으로 추가한다.

### tests

- [x] 주문 생성 도메인 테스트를 추가한다.
- [x] 주문 총액 계산 도메인 테스트를 추가한다.
- [x] 주문 생성 서비스 테스트를 추가한다.
- [x] 내 주문 목록 서비스 테스트를 추가한다.
- [x] 내 주문 상세 서비스 테스트를 추가한다.
- [x] 주문 생성 API 통합 테스트를 추가한다.
- [x] 내 주문 목록 API 통합 테스트를 추가한다.
- [x] 내 주문 상세 API 통합 테스트를 추가한다.

## Phase 4. 주문 수정, 삭제, 상태 전이 정리

### order domain

- [x] `OrderStatus`를 `CREATED`, `PAYMENT_PENDING`, `PAID`, `CANCELED`, `DELIVERED` 기준으로 정리한다.
- [x] `OrderItemStatus`를 주문 상태와 정합성 있게 정리한다.
- [x] 주문 수정 가능 여부 판단 메서드를 추가한다.
- [x] 주문 삭제 가능 여부 판단 메서드를 추가한다.
- [x] 주문 취소/삭제 상태 전이 메서드를 추가한다.

### order application

- [x] 주문 수정 command/DTO를 정의한다.
- [x] 주문 수정 서비스 유스케이스를 추가한다.
- [x] 주문 삭제 서비스 유스케이스를 추가한다.
- [x] 결제 완료 주문 수정 차단 로직을 추가한다.
- [x] 결제 완료 주문 삭제 차단 로직을 추가한다.

### order persistence

- [x] 소프트 삭제를 사용할지 결정한다.
- [x] 소프트 삭제를 사용하면 `deletedAt` 또는 `isDeleted` 필드를 추가한다.
- [x] 주문 수정 시 전체 주문상품 재구성 저장 로직을 검증한다.

### order adapter/in

- [x] `PATCH /api/orders/{orderId}` 컨트롤러를 추가한다.
- [x] `DELETE /api/orders/{orderId}` 컨트롤러를 추가한다.

### tests

- [x] 주문 수정 도메인 테스트를 추가한다.
- [x] 주문 삭제 도메인 테스트를 추가한다.
- [x] 주문 상태 전이 도메인 테스트를 추가한다.
- [x] 주문 수정 서비스 테스트를 추가한다.
- [x] 주문 삭제 서비스 테스트를 추가한다.
- [x] 주문 수정 API 통합 테스트를 추가한다.
- [x] 주문 삭제 API 통합 테스트를 추가한다.

## Phase 5. 결제 모듈과 토스 체크아웃 준비

### payment domain

- [x] `Payment` 엔티티를 추가한다.
- [x] `PaymentStatus` enum을 추가한다.
- [x] `PaymentMethod` enum을 추가한다.
- [x] `Payment`에 `paymentKey`, `providerReference`, `failureReason` 필드를 반영한다.
- [x] 주문당 활성 결제 1건 제약을 모델에 반영한다.

### payment application

- [x] 결제 생성 command/DTO를 정의한다.
- [x] 결제 상세 DTO를 정의한다.
- [x] 주문 기준 결제 생성 서비스 유스케이스를 추가한다.
- [x] 결제 상세 조회 서비스 유스케이스를 추가한다.
- [x] 결제 생성 시 주문 상태를 `PAYMENT_PENDING`으로 변경한다.
- [x] 주문 금액과 결제 금액 정합성 검증 로직을 추가한다.

### toss checkout preparation

- [x] 토스 체크아웃 응답 필드를 정의한다.
- [x] `orderId`에 사용할 내부 주문번호 형식을 최종 확정한다.
- [x] `orderName` 생성 규칙을 정한다.
- [x] `customerKey` 생성 규칙을 정한다.
- [x] `successUrl`, `failUrl` 설정값 주입 방식을 정한다.

### payment persistence

- [x] `PaymentRepository`와 JPA repository를 추가한다.
- [x] 결제 상세 조회 쿼리를 추가한다.
- [x] 주문-결제 관계 저장 방식을 정한다.
- [x] 활성 결제 중복 방지 제약을 DB 레벨에서 검토한다.

### payment adapter/in

- [x] `POST /api/orders/{orderId}/payments` 컨트롤러를 추가한다.
- [x] `GET /api/payments/{paymentId}` 컨트롤러를 추가한다.
- [x] 결제 관련 엔드포인트를 인증 대상으로 추가한다.

### payment adapter/out

- [x] 토스 API 설정 프로퍼티 클래스를 추가한다.
- [x] 토스 연동용 공통 헤더 구성 방식을 정한다.

### optional manual test support

- [x] 테스트용 체크아웃 HTML 페이지가 필요한지 결정한다.
- [x] 필요하면 `src/main/resources/static` 아래 샘플 페이지를 추가한다.

### tests

- [x] 결제 생성 도메인 테스트를 추가한다.
- [x] 결제 생성 서비스 테스트를 추가한다.
- [x] 결제 상세 조회 서비스 테스트를 추가한다.
- [x] 결제 생성 API 통합 테스트를 추가한다.
- [x] 결제 상세 API 통합 테스트를 추가한다.

## Phase 6. 토스 승인, 취소, 실패 처리

### toss api client

- [ ] 토스 승인 요청/응답 DTO를 정의한다.
- [ ] 토스 취소 요청/응답 DTO를 정의한다.
- [ ] 토스 API 클라이언트를 구현한다.
- [ ] Basic 인증 헤더 생성 로직을 추가한다.
- [ ] 토스 API 예외 변환 로직을 추가한다.

### payment application

- [ ] 결제 확정 command/DTO를 정의한다.
- [ ] 결제 취소 command/DTO를 정의한다.
- [ ] `POST /api/payments/{paymentId}/confirm` 유스케이스를 추가한다.
- [ ] `POST /api/payments/{paymentId}/cancel` 유스케이스를 추가한다.
- [ ] 토스 리다이렉트의 `orderId`, `amount` 검증 로직을 추가한다.
- [ ] 토스 승인 성공 시 `paymentKey` 저장 로직을 추가한다.
- [ ] 토스 승인 성공 시 주문 상태를 `PAID`로 변경한다.
- [ ] 토스 승인 실패 시 결제를 `FAILED`로 변경한다.
- [ ] 토스 승인 실패 시 주문을 재결제 가능 상태로 복구한다.
- [ ] 토스 취소 성공 시 결제 상태를 `CANCELED`로 변경한다.
- [ ] 토스 취소 후 주문 상태 정책을 최종 확정하고 반영한다.

### payment adapter/in

- [ ] `POST /api/payments/{paymentId}/confirm` 컨트롤러를 추가한다.
- [ ] `POST /api/payments/{paymentId}/cancel` 컨트롤러를 추가한다.

### tests

- [ ] 토스 API 클라이언트 직렬화/역직렬화 테스트를 추가한다.
- [ ] 결제 확정 서비스 테스트를 추가한다.
- [ ] 결제 취소 서비스 테스트를 추가한다.
- [ ] 토스 승인 실패 서비스 테스트를 추가한다.
- [ ] 결제 확정 API 통합 테스트를 추가한다.
- [ ] 결제 취소 API 통합 테스트를 추가한다.

## Phase 7. 안정화와 운영 준비

### configuration and security

- [ ] 토스 관련 환경변수를 `application.yml` 또는 설정 클래스에 반영한다.
- [ ] 프로파일별 설정 방식을 정리한다.
- [ ] 보안상 로그에 남기면 안 되는 필드를 정리한다.
- [ ] 외부 API 오류와 도메인 오류 매핑 규칙을 정리한다.

### observability and docs

- [ ] 주문 상태 변경 로그를 추가한다.
- [ ] 결제 상태 변경 로그를 추가한다.
- [ ] 토스 호출 실패 로그 마스킹 규칙을 적용한다.
- [ ] 로컬/테스트베드 실행 가이드를 문서화한다.
- [ ] 토스 테스트 결제 절차를 문서화한다.

### tests and architecture

- [ ] `ModulithArchitectureTests`를 현재 모듈 구조에 맞게 갱신한다.
- [ ] `payment` 모듈 공개 API/의존 방향을 아키텍처 테스트에 반영한다.
- [ ] 전체 영향 범위를 확인하기 위한 테스트 실행 계획을 정리한다.
- [ ] 주요 시나리오 회귀 테스트 목록을 만든다.

## 마무리 체크

- [ ] 상품 목록/상세가 좋아요와 함께 정상 조회된다.
- [ ] 주문 생성/조회/수정/삭제가 본인 주문 기준으로 동작한다.
- [ ] 토스 테스트베드로 결제 생성/확정/취소가 동작한다.
- [ ] 리뷰 기능을 건드리지 않고 전체 흐름이 유지된다.
- [ ] 필요한 단위 테스트, 통합 테스트, Modulith 테스트가 추가된다.
