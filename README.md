## Profiles

- `application.yml`: 공통 설정과 운영용 환경변수 placeholder를 둔다.
- `application-test.yml`: 통합 테스트용 토스/메일/JWT 기본값을 둔다.
- 로컬 실행은 환경변수를 주입한 뒤 `./gradlew bootRun`을 사용한다.

## Required Env

- `JWT_SECRET`, `JWT_ACCESS_EXPIRATION_MS`, `JWT_REFRESH_EXPIRATION_MS`
- `SMTP_HOST`, `SMTP_PORT`, `EMAIL`, `APP_PASSWORD`
- `TOSS_PAYMENTS_BASE_URL`, `TOSS_PAYMENTS_CLIENT_KEY`, `TOSS_PAYMENTS_SECRET_KEY`
- `TOSS_PAYMENTS_SUCCESS_URL`, `TOSS_PAYMENTS_FAIL_URL`

## Logging Rules

- 로그에 남기지 말아야 하는 값: `JWT_SECRET`, 토스 `secretKey`, `Authorization` 헤더, `paymentKey`, refresh token
- 토스 호출 실패 로그는 `paymentKey`, `orderId`를 앞 2자리와 뒤 2자리만 남기고 마스킹한다.
- 주문 상태 변경은 `orderId`, 이전 상태, 이후 상태를 남긴다.
- 결제 상태 변경은 `paymentId`, `orderId`, 상태, provider reference만 남긴다.

## Error Mapping

- 주문/결제 금액 검증 실패, 상태 전이 실패, 권한 문제는 `BusinessException`으로 처리하고 도메인 `ErrorCode`를 그대로 응답한다.
- 토스 승인/취소 호출 자체가 실패하면 `EXTERNAL_PAYMENT_REQUEST_FAILED`로 변환하고 HTTP `502 Bad Gateway`를 반환한다.
- 토스 승인 실패 후에는 결제를 `FAILED`로 바꾸고 주문은 다시 `CREATED`로 돌려 재결제를 허용한다.
- 토스 취소 성공 후에는 결제를 `CANCELED`, 주문을 `CANCELED`로 정리한다.

## Local Flow

1. 필수 환경변수를 설정한다.
2. `compose.yaml`로 필요한 인프라를 올린다.
3. `./gradlew bootRun`으로 애플리케이션을 실행한다.
4. 회원가입, 로그인 뒤 상품 조회와 주문 생성 API를 호출한다.
5. `POST /api/orders/{orderId}/payments`로 체크아웃 파라미터를 만든다.

## Toss Testbed

1. 결제 생성 응답의 `checkout.clientKey`, `checkout.orderId`, `checkout.orderName`, `checkout.customerKey`, `checkout.amount`를 토스 테스트 결제창 초기화 값으로 사용한다.
2. `successUrl`로 리다이렉트되면 토스가 넘겨준 `paymentKey`, `orderId`, `amount`를 `POST /api/payments/{paymentId}/confirm`에 그대로 전달한다.
3. 승인 성공 후 `GET /api/payments/{paymentId}`와 `GET /api/orders/{orderId}`로 결제/주문 상태가 `CONFIRMED`, `PAID`인지 확인한다.
4. 환불 또는 취소 검증은 `POST /api/payments/{paymentId}/cancel`에 취소 사유를 보내고 상태가 `CANCELED`인지 확인한다.
5. 별도 정적 HTML 테스트 페이지는 유지하지 않고 API 응답과 토스 테스트베드만으로 검증한다.

## Test Plan

- 주문/결제 모듈 변경 시 우선 실행: `./gradlew test --tests 'zoonza.commerce.order.**' --tests 'zoonza.commerce.payment.**'`
- 보안 경로 변경 시 추가 실행: `./gradlew test --tests 'zoonza.commerce.auth.adapter.in.AuthSecurityIntegrationTest'`
- 모듈 경계 확인 시 실행: `./gradlew test --tests 'zoonza.commerce.ModulithArchitectureTests'`
- 전체 영향 확인 전 최종 실행: `./gradlew test`

## Regression Checklist

- 상품 목록/상세의 좋아요 집계와 로그인별 `likedByMe`가 유지된다.
- 주문 생성/조회/수정/삭제가 본인 주문 기준으로 동작한다.
- 결제 생성 시 주문 상태가 `PAYMENT_PENDING`으로 바뀐다.
- 토스 승인 성공 시 결제가 `CONFIRMED`, 주문이 `PAID`가 된다.
- 토스 승인 실패 시 결제가 `FAILED`, 주문이 다시 `CREATED`가 된다.
- 토스 취소 성공 시 결제와 주문이 모두 `CANCELED`가 된다.
- 리뷰 작성 흐름이 구매 확정 주문상품 기준으로 유지된다.
