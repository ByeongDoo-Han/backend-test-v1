# PG 연동 결제 시스템

## 1.프로젝트 개요

프로젝트는 비즈니스 로직과 외부 기술을 분리하는 헥사고날 아키텍처(Ports and Adapters)를 기반으로 설계된 결제 서버입니다.
이를 통해 외부 환경(UI, DB, 외부 API)의 변화가 핵심 비즈니스 로직에 미치는 영향을 최소화하고, 코드의 테스트 용이성을 높였습니다.

- **domain**: 순수한 비즈니스 모델과 규칙을 포함하며, 다른 레이어에 대한 의존성이 없는 핵심 영역입니다.
- **application**: 사용자의 유스케이스를 정의하고 비즈니스 흐름을 관장합니다. `domain` 레이어에 의존하여 비즈니스 로직을 조합합니다.
- **bootstrap**: 애플리케이션을 실행하고 API 엔드포인트를 노출하는 등 각 모듈을 조합하고 설정하는 역할을 합니다.
- **infrastructure**: 데이터베이스 연동 등 내부 시스템과 관련된 기술(Inbound/Outbound Adapter)을 구현합니다.
- **external**: 외부 PG사 API 연동 등 외부 시스템과 관련된 기술을 구현합니다.

**Swagger** : [link](http://localhost:8080/swagger-ui/index.html)

**Frontend** : [link](http://localhost:3000/)

**Monitoring** : [link](http://localhost:3001/d/spring_boot_21/spring-boot-3-x-statistics?orgId=1&from=now-1h&to=now&timezone=browser&var-application=&var-Namespace=&var-instance=bigs-app:8080&var-hikaricp=HikariPool-1&var-memory_pool_heap=$__all&var-memory_pool_nonheap=$__all)

## 2.API 엔드포인트

1. 결제 생성

- 엔드포인트: `POST /api/v1/payments`

* 내용 : 필요 정보를 통해 결제 승인 후, 수수료/정산금 계산 결과를 포함하여 결제 내용을 저장합니다.
    * `POST /api/v1/payment`
    * `body` :
        * `partnerId` : 결제 제휴사 아이디
        * `amount` : 결제 금액
        * `productName` : 결제 제품 이름
        * `cardBin` : 카드 앞 6자리
        * `cardLast4` : 카드 뒤 4자리
    * 응답 :
        * `id` : 결제 객체 아이디
        * `partnerId` : 결제 제휴사 아이디
        * `amount` : 결제 금액
        * `appliedFeeRate` : 수수료 정책
        * `feeAmount` : 수수료
        * `netAmount` : 결제 금액 중 수수료를 제외한 금액
        * `cardLast4` : 카드 뒤 4자리
        * `approvalCode` : 승인 코드
        * `approvedAt` : 승인 시간
        * `status` : 승인 상태
        * `createdAt` : 결제 생성 시간

2. 결제 내역 조회 + 통계

- 엔드포인트: `GET /api/v1/payments`

* 내용 : 요구 쿼리를 통해 일치 하는 아이템, 요약 내용, 다음 커서 조회
    * `GET /api/v1/payment`
    * `param` :
        * `partnerId` : 파트너 아이디
        * `status` : 결제 승인 상태
        * `from` : 시작 범위
        * `to` : 종료 범위
        * `cursor` : 다음 커서 정보
        * `limit` : 커서 당 조회 갯수
    * 응답 :
        * `items[]`, `summary{count,totalAmount,totalNetAmount}`, `nextCursor`, `hasNext`

3. 결제 요청 (추가 - 카드 번호 입력 시나리오 적용)

- 엔드포인트: `POST /api/v1/payments/buy`

* 내용 : 카드 정보 입력을 통해 결제 승인(외부 PG 연동) 후, 수수료/정산금 계산 결과를 포함하여 저장
    * `POST /api/v1/payment`
    * `body` :
        * `cardNumber` : 카드 번호
        * `birthDate` : 생일(YYYYMMDD)
        * `expiry` : 만료일자(MMDD)
        * `password` : 비밀번호 앞 두자리
        * `partnerId` : 결제 제휴사 아이디
        * `amount` : 결제 금액
        * `productName` : 결제 제품 이름
    * 응답 :
        * `id` : 결제 객체 아이디
        * `partnerId` : 결제 제휴사 아이디
        * `amount` : 결제 금액
        * `appliedFeeRate` : 수수료 정책
        * `feeAmount` : 수수료
        * `netAmount` : 결제 금액 중 수수료를 제외한 금액
        * `cardLast4` : 카드 뒤 4자리
        * `approvalCode` : 승인 코드
        * `approvedAt` : 승인 시간
        * `status` : 승인 상태
        * `createdAt` : 결제 생성 시간

## 3.기술 스택

| 구분            | 기술                             | 버전/내용                      |
|---------------|--------------------------------|----------------------------|
| **백엔드**       | Kotlin, Spring Boot            | Java 21, Spring Boot 3.2.7 |
|               | Spring Data JPA                |                            |
|               | Spring Boot Actuator           | 애플리케이션 모니터링 및 메트릭 노출       |
| **프론트엔드**     | React, TypeScript              |                            |
| **데이터베이스**    | MySQL                          | 8.0                        |
| **빌드 도구**     | Gradle                         |                            |
| **API 클라이언트** | Spring WebClient               | 외부 PG사 API 비동기 호출          |
| **테스트**       | MockK                          |                            |
| **문서화**       | Springdoc OpenAPI (Swagger UI) | 2.5.0                      |
| **컨테이너화**     | Docker, Docker Compose         | Docker 28.0.4              |
| **모니터링**      | Prometheus, Grafana            |                            |
| **기타**        | Slf4j                          | logging                    |

## 4. 실행 방법

### 4.1 사전 요구사항

- Docker 및 Docker Compose가 설치되어 있어야 합니다.
- 설치되지 않았다면 4.1.1 을 참고해 Docker를 설치할 수 있습니다.

### 4.1.1 Docker 설치하기

💻 Windows / macOS / Linux 사용자 공통

1. Docker Desktop 설치
    1. https://www.docker.com/products/docker-desktop/ 접속
    2. OS에 맞는 Docker Desktop 설치
    3. 설치 후 실행 (최초 설치 시 로그인 필요 – 무료 계정 생성 가능)

윈도우 사용자는 반드시 WSL2가 설치되어 있어야 합니다.

	•   설치 가이드: https://learn.microsoft.com/ko-kr/windows/wsl/install

### 4.1.2

macOS 터미널에서 설치하기

```bash
brew install docker
brew install docker-compose
```

### 4.1.3 설치 확인

터미널 (cmd, powershell, 터미널 등)에서 아래 명령 실행

```
docker version
docker compose version
```

출력 예시

```
Docker version 28.0.4, build cb74dfc
Docker Compose version v2.34.0
```

### 4.2. 애플리케이션 실행

프로젝트 클론

```
git clone https://github.com/ByeongDoo-Han/backend-test-v1
```

디렉토리 이동 && 실행 권한 부여 && 프로젝트 빌드

```
cd backend-test-v1
chmod +x ./gradlew
./gradlew clean build
```

프로젝트 루트 디렉토리에서 아래 명령어를 실행하여 애플리케이션 서버와 데이터베이스를 한 번에 실행합니다.

```bash
  docker compose up --build
```

- `--build` 옵션은 최초 실행 시 또는 코드 변경 사항이 있을 때 이미지를 새로 빌드하기 위해 사용합니다.

### 4.3. API 문서 확인

애플리케이션이 정상적으로 실행된 후, 웹 브라우저에서 아래 주소로 접속하여 Swagger UI를 통해 API 문서를 확인하고 직접 테스트할 수 있습니다.

- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## 5. 명령어 사용 방법

프로젝트의 단위 테스트 및 통합 테스트를 실행하려면 아래 명령어를 사용합니다.

```bash
  ./gradlew test
```

컴파일 + 모든 테스트를 실행하려면 아래 명령어를 사용합니다.

```bash
  ./gradlew clean build
```

API 동작을 실행하려면 아래 명령어를 사용합니다.

```bash
  ./gradlew :modules:bootstrap:api-payment-gateway:bootRun
```

코드 스타일 검사 및 자동정렬하려면 아래 명령어를 사용합니다.

```bash
  ./gradlew ktlintCheck | ktlintFormat
```

## 5. 주요 설계 결정사항

### 5.1. 결제 시스템 추상화

이 프로젝트의 결제 시스템은 다양한 결제 제휴사를 유연하게 통합하고 관리하기 위해 추상화 설계를 도입했습니다.

- application - pg.port.out - `PgClientOutPort`
- 모든 결제 제휴사 구현체에서 구현해야 하는 표준 기능을 정의합니다. supports(partnerId) 메서드를 통해 특정 해당 파트너가 결제를 지원하는 지 확인하고, approve(request) 메서드를 통해
  결제 요청 중 승인 로직을 실행합니다.
- 이를 통해 새로운 결제 수단이 추가되거나 기존 결제 수단의 구현이 변경되어도 PaymentService와 같은 상위 계층의 코드를 최소한으로 변경할 수 있습니다.
- 구현체 분리 - `MockPgClient`(기본 제공 구현체), `TestPgClient`(API 연동 구현체)
    - external - pg - [`MockPgClient`, `TestPgClient`] 와 같이 각 결제 제휴사별로 `PgClientOutPort`를 구현하는 클래스로 분리했습니다.
    - 각 구현체는 해당 제휴사의 승인 로직을 캡슐화합니다.
- application - payment - service - `PaymentService`
    - 클라이언트로부터 받은 `PaymentCommand`를 기반으로 적절한 `PgClientOutPort` 구현체를 찾아 결제 승인을 위임합니다.

### 5.2. Prometheus & Grafana

- 
- **Spring Boot Actuator**: 백엔드 애플리케이션의 다양한 메트릭(JVM, CPU, HTTP 요청 등)을 외부에 노출합니다.
- **Prometheus**: Actuator가 노출한 메트릭을 주기적으로 수집(pull)하고 시계열 데이터로 저장합니다.
- **Grafana**: Prometheus가 수집한 데이터를 시각화하여 대시보드를 통해 보여줍니다.

### 5.3. 프론트엔드 구현

- 사용자가 카드 정보를 입력해서 결제를 요청하는 상황을 가정해 결제 페이지를 구현했습니다. 
- 성공하지 않는 카드 번호를 입력해서 결제 실패 응답을 확인할 수 있습니다.
- Test PG API 연동 문서를 활용해 `EncryptionUtil` 에 pg사에 전달할 내용을 암호화하도록 구현했습니다.

### 5.4. Swagger 적용

- 컨트롤러에 직접 Swagger 설정값과 example을 적용했을 때 코드가 길어지고 가독성을 해친다고 판단하여 `PaymentApiDocs` 인터페이스에 Swagger 설정을 지정하고 이를 구현하도록 구성했습니다. 

### 5.5 Docker 컨테이너화
### 

이 프로젝트는 헥사고날 아키텍처(Hexagonal Architecture, Ports and Adapters)를 기반으로 설계되었습니다. 각 모듈의 역할은
다음과 같습니다.

* domain: 순수한 비즈니스 모델과 핵심 규칙을 포함하는, 의존성이 없는 가장 내부적인 레이어입니다.
* application: 사용자의 요청을 처리하는 유스케이스(Use Case)를 정의하고, 비즈니스 흐름을 관장하는 레이어입니다.
* common: 여러 모듈에서 공통으로 사용되는 유틸리티, DTO, 예외 클래스 등을 포함합니다.
* infrastructure: 데이터베이스 연동 등 내부 시스템과 관련된 기술을 구현하는 어댑터가 위치합니다.
* external: 외부 API 연동 등 외부 시스템과 관련된 기술을 구현하는 어댑터가 위치합니다.
* bootstrap: 각 모듈을 조합하여 애플리케이션을 실행하고, 웹 API를 노출하는 레이어입니다.
