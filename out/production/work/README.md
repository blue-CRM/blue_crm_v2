# Blue CRM — 2차 개발

> 콜(전화) 운영·영업 조직을 위한 CRM. Spring Boot 3 + Vue 3, MySQL 8, Redis 기반. 본 문서는 **2차 개발용 레포** 기본 안내입니다.

---

## 🔎 개요

* **백엔드**: Spring Boot 3.x (Java 17), Gradle, MyBatis, MySQL 8(RDS), Redis(ElastiCache)
* **프런트**: Vue 3 + Vite + Tailwind
* **배포**: EC2, Nginx 정적 서빙, `/api` 백엔드 프록시
* **인증**: JWT(Access 15분, Refresh 12시간·쿠키/서버관리)
* **동기화**: Google Sheets → Scheduler(매분), Redis 분산락, 증분 커서

### 핵심 도메인/정책 요약

* `customers`에는 **최초/유효 고객만** 저장, 30일 이내 동일 전화는 **중복** 처리 → `customers_duplicate` 저장
* 분배/회수 권한: **본사→센터**, **센터→직원**, 재분배는 반드시 **회수 후** 수행

---

## 📁 백엔드+프론트 폴더 구조

```
backend/src/main/java/com/blue/
├─ auth/
├─ customer/
│  ├─ all/
│  ├─ allocate/
│  ├─ center/
│  ├─ common/
│  │  ├─ center/
│  │  ├─ memo/
│  │  └─ sheets/
│  ├─ duplicate/
│  └─ revoke/
├─ dashboard/
├─ global/
├─ info/
└─ user/

backend/src/main/resources/
└─ application*.yml, mappers/**/*, etc.

forntend/src/
├─ components/
│  ├─ layout/
│  ├─ profile/
│  ├─ tables/
│  └─ ui/
├─ composables/
├─ router/
├─ stores/
└─ views/
```

---

## 🧰 기술 스택

* **Backend**: Spring Boot 3.x, Java 17, Gradle, MyBatis, MySQL 8, Redis(SSL 가능)
* **Frontend**: Vue 3, Vite, Tailwind
* **Infra**: Nginx, EC2, RDS, Redis
* **Others**: Google Sheets API(Service Account), Spring Scheduler, Actuator

---

## 🚀 빠른 시작 (로컬 개발)

### 0) 요구 사항

* Java 17, Node 18+, MySQL 8, Redis, Git

### 1) 데이터베이스/Migration

```sql
CREATE DATABASE bluecrm CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```
* DB 스키마는 부록A 참고

### 2) 백엔드 설정

`backend/src/main/resources/application-local.yml` (예시)

```yaml
spring.application.name=blue

# MyBatis
mybatis.mapper-locations=classpath*:mapper/**/*.xml
mybatis.type-aliases-package=com.blue.**.domain

# JWT key
jwt.access.secret=
jwt.refresh.secret=

# cors
app.cors.enabled=true
app.cors.allowed-origins=http://localhost:5173

# cookie
jwt.cookie.secure=false

# Java Mail Sender
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=
spring.mail.password=
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Logging
logging.level.com.blue.global.security.JwtAuthenticationFilter=DEBUG
logging.level.org.springframework.security=WARN
logging.level.org.springframework.web=WARN

# google - window
google.creds.location=file:keys/

# DB
spring.datasource.url=jdbc:mysql://localhost:3306/blue?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.timeout=6000
```

---

## 🔐 보안 & 인증

### Spring Security 설정 요약

* **세션**: STATELESS (JWT)
* **공개**: `/actuator/health`, `/api/auth/**`, `/api/mail/**`
* **인증 필요**: `/api/ping`, `/api/common/**`, `/api/work/**`, `/api/info/**`, `/api/sheets/**`
* **역할 매핑**

  * `ROLE_SUPERADMIN`: `/api/super/**`
  * `ROLE_MANAGER`: `/api/admin/**`
  * `ROLE_STAFF`: `/api/staff/**`
* **JWT 필터**: `Authorization: Bearer <access>` 검사 → 승인되지 않은 계정은 `410 Gone` + `X-Blocked: true`
* **만료 시**: 401 + `WWW-Authenticate`, `X-Token-Expired: 1`

### CORS 설정

* `app.cors.enabled=true` 인 경우만 활성
* 허용 Origin: `app.cors.allowed-origins` (콤마 구분, `addAllowedOriginPattern` 지원)
* Credentials 허용, Methods: GET/POST/PUT/PATCH/DELETE/OPTIONS, 일부 헤더 노출(`X-Blocked` 등)

### JWT

* **Access**: 15분(HS256, `jwt.access.secret`)
* **Refresh**: 12시간(HS256, `jwt.refresh.secret`, `jti` 포함)
* **Claims**: `sub=email`, `role`, `name`, `isSuper`
* **키 주입**: `JwtKeys`가 Base64 시크릿 2종을 HMAC 키로 변환

### 로그인 로그 (`login_logs`)

* 로그인: INSERT (스냅샷 이름/전화/권한)
* 로그아웃: 가장 최근 미종료 건 `logout_at=NOW()`
* Refresh 회전: `session_key`(=jti) 교체 업데이트 지원

---

## 📊 대시보드 API (공통)

**Base**: `/api/common/dashboard`

* `GET /users` → 승인(Y) 사용자 요약 목록
* `GET /users/find?name=정확이름 | email=정확이메일` → 정확 일치 검색
* `GET /customers?userId&centerIds=1,2&from=YYYY-MM-DD&to=YYYY-MM-DD` → 본체+중복 통합 조회
  *Note: duplicate 쿼리는 **`duplicate_display=1`** 권장. 불필요한 **`status!='없음'`** 필터 주의.*
* `GET /kpi` == `GET /summary` → KPI(승인 사용자 수, 센터 수[본사 제외])

---

## 🧱 사용자/조직도 API

### SUPER 전용 사용자 관리

**Base**: `/api/super/users`

* `GET /` → 페이지 목록 (page/size/keyword)
* `GET /has-manager?centerName&excludeUserId` → 해당 센터에 **다른** MANAGER 존재 여부
* `PATCH /update/{userId}` `{ field, value }` → 배지 수정

  * 보호 규칙:

    * 본인 계정 수정 금지
    * SUPERADMIN/본사 대상은 요청자가 SUPER일 때만
    * `visible`(가시권한)은 SUPER만
    * `type='센터장'` 승격 시, 해당 센터 기존 MANAGER 있으면 거부
    * `center` 이동 시 대상이 MANAGER면, 도착 센터 기존 MANAGER 있으면 거부
  * 역할 변경 후 자동 회수: **MANAGER → STAFF** 또는 **MANAGER → SUPERADMIN** 시 `status='없음'`인 DB 회수
* `PATCH /bulk-approve` `[userId...]` → 일괄 승인 (SUPER/본사 대상 포함 시 SUPER만 가능)

### 조직도/정보

**Base**: `/api/info`

* `GET /tree` → HQ/센터/사용자 트리 + `currentUser`

  * 가시 범위: SUPER/HQ=전체, SUPER(비HQ)/MANAGER=자기 센터, STAFF=본인만
  * 정렬: 권한(관리자>센터장>담당자) → 입사일
* `GET /centers` → 센터명 목록
* `PATCH /users/update/{userId}` `{ field: name|phone, value }` → 이름/전화 수정 (권한 검사 동일)

---

## 📝 메모 API

**Base**: `/api/work/db/memo`

* `GET /{customerId}` → 메모 상세 + 과거 담당자 이력(`phone`으로 조회)
* `PATCH /{customerId}` `{ memo, status, promiseTime, tradingviewId, telegramNickname, freeRoom, signalRoom, exchangeJoined, tradingviewJoined, indicatorFlag }`

  * 빈 문자열은 `null` 정규화, 정수 플래그는 null→0
  * 권한: SUPER=전체, MANAGER=자기 센터 보유건, STAFF=본인 보유건
  * 중복DB는 수정 불가 (에러 메시지 주의)

---

## 🔁 Google Sheets 동기화

**엔드포인트**: `/api/sheets`

* `POST /refresh?sid=1&full=false` → **수동 증분** (유지보수 창에서는 차단)

  * 레이트: `1초 디바운스`, `분당 59회 상한`
* `GET /cursor?sid=1` → 소스 커서/메타 조회

**스케줄러**

* 01:00 `nightlyResume()` → 커서 기준 **드레인** 수행 (lookback 비활성화)
* 01:00–23:59 매분 `autoRefresh()` → 자동 증분

**락/키 전략(예시)**

* 잡락: `gsync:job:{sid}` (30s)
* 분당 1회 보장: `gsync:auto:lock:{sid}:{yyyyMMddHHmm}` (120s)
* 수동 디바운스: `gsync:lock:{sid}` (1s)
* 수동 레이트: `gsync:rate:{sid}` (TTL 60s)

**증분 규칙**

* 커서 다음 행부터 A~I 범위 로드, **앞에서부터 완전행(연속)만** 처리 후 커서 전진
* 완전행 기준:

  * A(생성일): `yyyy. M. d 오전/오후 h:mm[:ss]` (KST) 등 파서 지원
  * C(이름) 필수, E(전화) 한국 포맷 정규화 가능해야 함, I(출처) 필수
* 30일 규칙:

  * 동일 전화 **30일 이내** 본체가 있으면 **duplicate** 삽입(기본 `duplicate_display=1`)
  * 없으면 본체 삽입(`division=최초|유효`, `status='없음'`, `category='주식'`)
* **중복 삽입 방지**: 본체/중복 각각 **모든 필드 동일** 이벤트는 스킵

**전화 포맷팅**

* `02` 지역: 9자리 → `02-XXX-XXXX`, 10자리 → `02-XXXX-XXXX`
* 휴대/지역 `0` 시작: 10자리 → `3-3-4`, 11자리 → `3-4-4`
* 8자리 → `010-` 접두 후 `3-4-4`

**Google API 설정**

* `google.creds.location` (예: `file:./secrets/google-sa.json`)
* Sheets 클라이언트 스코프: `SPREADSHEETS_READONLY`

---

## 🏃‍♂️ 프런트 개요

**Axios 인스턴스 요약**

* `baseURL = import.meta.env.VITE_API_BASE || '/api'`
* `Authorization: Bearer <access>` 자동 주입
* `401` 처리: `/api/auth/token/refresh` **단일비행(single‑flight)** → 원요청 **1회 재시도**
* 재발급 실패 시: `/api/auth/token/logout` 호출 후 **로그인 화면으로**

**라우터 가드 요약**

* `meta.requiresAuth=true` 라우트는 **토큰 필수**
* `meta.roles=['SUPERADMIN'|'MANAGER'|'STAFF']` 권한 체크
* 응답이 `410 Gone` + `X-Blocked:true` 면 **차단 안내 페이지**로 리다이렉트

**유지보수 배너**

* `/api/ping` 또는 응답 헤더 `X-Maintenance:true` 검출 시 **상단 배너 노출**
* **화이트리스트 패턴(예)**: `^/api/(auth|mail|sheets/refresh)$`

**UI 규칙**

* 긴 작업은 `runBusy()`로 감싸고, 완료 후 **선택 초기화**
* 전화 마스킹은 **SUPER & visible='N'** 조건에서만 적용

---

## 🏗️ 빌드 & 배포

### 프런트 (정적 빌드)

```bash
cd frontend
npm run build
```

### 백엔드 (systemd 서비스 예)

```ini
# /etc/systemd/system/blue.service
[Unit]
Description=Blue CRM Backend
After=network.target

[Service]
User=ec2-user
WorkingDirectory=/opt/blue
ExecStart=/usr/bin/java -jar app.jar
Restart=always
Environment="JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8"

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now blue
journalctl -u blue -f
```

### Nginx 프록시(단일 서버)

```nginx
server {
  listen 80;
  server_name _;

  root /var/www/blue-frontend;
  index index.html;

  location / { try_files $uri /index.html; }
  location /api/ {
    proxy_pass http://127.0.0.1:8080/; # 분리 시: http://<backend-private-ip>:8080/
    proxy_set_header Host $host;
    proxy_set_header X-Forwarded-For $remote_addr;
  }
}
```

---

## 🧩 주요 테이블

* `centers` — 센터 리스트
* `users` — 직원 계정 (권한: SUPERADMIN / MANAGER / STAFF)
* `customers` — 최초/유효 고객 (30일 규칙: phone + created_at)
* `customers_duplicate` — 중복 고객 (항상 **본체** `customer_id` 참조)
* `customer_past_users` — 과거 담당자 이력(신규 상태 고객은 없음)
* `gsheet_sources` — 스프레드시트 동기화 관리(파일ID, 시트명, 커서행, updated_at)

---

## 🗺️ 로드맵(1차 개발 범위)

* 고객 CRUD, 중복DB 정책 준수 UI/백엔드
* 분배/회수 플로우(본사/센터/직원 단계)
* 구글시트 증분 동기화, 관리자 수동 새로고침
* 기본 대시보드(카운트/스냅샷)

---

## 🤝 기여 규칙

* 브랜치: `feature/*`, `fix/*`, `release/*`
* PR 템플릿: 목적/변경점/테스트/릴리스노트
* 커밋: Conventional Commits 권장(선택)

---

## 📄 라이선스

* 내부 프로젝트(기본값). 필요 시 라이선스 명시.

---

## 부록 A. DB 스키마

```properties
/* =========================================
   초기화
========================================= */
SET NAMES utf8mb4;
SET time_zone = '+09:00';

SELECT @@global.time_zone, @@session.time_zone, NOW();

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS customers_duplicate;
DROP TABLE IF EXISTS customer_past_users;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS centers;
DROP TABLE IF EXISTS gsheet_sources;
DROP TABLE IF EXISTS login_logs;
DROP TABLE IF EXISTS phone_lookup;
SET FOREIGN_KEY_CHECKS = 1;

/* =========================================
   centers
========================================= */

CREATE TABLE centers (
     center_id   BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '센터 고유 ID',
     center_name VARCHAR(100) NOT NULL COMMENT '센터 이름(유니크)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='센터 마스터';

CREATE UNIQUE INDEX uq_centers_name ON centers(center_name);

INSERT INTO centers (center_id, center_name) VALUES
     (1,'본사'),
     (2,'센터A'),
     (3,'센터B');

/* =========================================
   users (직원)
========================================= */

CREATE TABLE users (
   user_id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '직원 ID',
   user_email           VARCHAR(100)  NOT NULL COMMENT '이메일(로그인 ID)',
   user_role            VARCHAR(20)   NOT NULL COMMENT '권한: SUPERADMIN(관리자)/MANAGER(센터장)/STAFF(담당자)',
   center_id            BIGINT        NULL COMMENT '소속 센터 ID(관리자면 보통 1=본사 또는 NULL)',
   user_name            VARCHAR(50)   NOT NULL COMMENT '이름',
   user_approved        CHAR(1)       NOT NULL DEFAULT 'N' COMMENT '승인 상태: Y=승인, N=대기, X=탈퇴',
   manager_phone_access CHAR(1)       NOT NULL DEFAULT 'N' COMMENT '센터장·담당자 전화번호 가시 권한(Y/N)',
   is_super             CHAR(1)       NOT NULL DEFAULT 'N' COMMENT '슈퍼계정 여부(Y/N)',
   user_phone           VARCHAR(20)   NOT NULL COMMENT '전화번호',
   user_password        VARCHAR(255)  NOT NULL COMMENT 'BCrypt 해시 비밀번호',
   user_created_at      DATETIME      NULL DEFAULT CURRENT_TIMESTAMP COMMENT '계정 생성 시각',
   CONSTRAINT uq_users_email UNIQUE (user_email),
   CONSTRAINT fk_users_center FOREIGN KEY (center_id) REFERENCES centers(center_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='직원 계정';

CREATE INDEX idx_users_created      ON users(user_created_at DESC);
CREATE INDEX idx_users_approved     ON users(user_approved);
CREATE INDEX idx_users_name         ON users(user_name);
CREATE INDEX idx_users_center       ON users(center_id, user_id);
CREATE INDEX idx_users_is_super ON users(is_super);

INSERT INTO users
(user_email,user_password,user_role,manager_phone_access,center_id,user_name,user_phone,user_approved,user_created_at, is_super) VALUES
     ('~~@naver.com','$~~UkMfPFAC','SUPERADMIN','Y',1,'대표계정','~~','Y',now(), 'Y'),
     ('~~@naver.com','$2a$1~~u','SUPERADMIN','Y',1,'~~','~~','Y',now(), 'N'),
     ('~~@naver.com','$2~~nz.66','SUPERADMIN','N',1,'~~','~~','Y',now(),'N'),
     ('~~@gmail.com','$2a$1~~urfCa~~G','SUPERADMIN','N',1,'~~','~~','Y',now(),'N'),
     ('~~@gmail.com','$2a~~MOU4iUW','SUPERADMIN','N',1,'~~','~~','Y',now(),'N'),
     ('~~@naver.com','$2~~K','SUPERADMIN','N',1,'~~','~~','Y',now(),'N'),
     ('~~@naver.com','$~~u','MANAGER','Y',2,'~~','010-0000-0000','Y',now(), 'N'),
     ('~~@naver.com','$2a$~~yJ8Fu','STAFF','Y',2,'~~','010-0000-0000','Y',now(), 'N'),
     ('~~@naver.com','$2a$~~8Fu','MANAGER','Y',3,'~~','010-0000-0000','Y',now(), 'N'),
     ('~~@naver.com','$2a~~Fu','STAFF','Y',3,'~~','010-0000-0000','Y',now(), 'N');

update users set is_super = 'Y'  where user_id = 1;

/* =========================================
   login_logs
========================================= */

CREATE TABLE login_logs (
    log_id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '로그 PK',
    session_key    VARCHAR(64) NULL COMMENT '세션 식별자',
    user_id        BIGINT      NOT NULL COMMENT '사용자 ID',
    snapshot_name  VARCHAR(50) NOT NULL COMMENT '이름 스냅샷',
    snapshot_phone VARCHAR(20) NOT NULL COMMENT '전화번호 스냅샷',
    snapshot_role  VARCHAR(20) NOT NULL COMMENT '역할 스냅샷',
    login_at       DATETIME    DEFAULT NULL COMMENT '로그인 시각',
    logout_at      DATETIME    DEFAULT NULL COMMENT '로그아웃 시각',
    CONSTRAINT fk_login_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT uk_login_session UNIQUE (session_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='로그인/로그아웃 성공 로그';

CREATE INDEX idx_login_at ON login_logs(login_at);
CREATE INDEX idx_login_user_at   ON login_logs (user_id, login_at DESC);

/* =========================================
   customers (유효/최초)
========================================= */

CREATE TABLE customers (
   customer_id                  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '유효/최초 고객 ID',
   user_id                      BIGINT NULL COMMENT '현재 담당자(없으면 미배정/회수 상태)',
   customer_created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '고객 레코드 생성 시각',
   customer_name                VARCHAR(255)  NOT NULL COMMENT '이름',
   customer_phone               VARCHAR(50)  NOT NULL COMMENT '전화번호(30일 규칙 판단 키)',
   customer_memo                TEXT NULL COMMENT '메모',
   customer_content             TEXT NULL COMMENT '상담/비고 내용',
   customer_source              VARCHAR(255) NULL COMMENT 'DB 출처',
   customer_division            VARCHAR(20)  NOT NULL COMMENT '구분: 최초/유효',
   customer_status              VARCHAR(20)  NOT NULL COMMENT '상태: 없음/신규/회수/완료/부재1~5, 재콜, 가망, 거절',
   customer_promise_time        DATETIME NULL COMMENT '약속 시간(콜백/방문 예약 등)',
   customer_category            VARCHAR(50)  NOT NULL COMMENT '카테고리(상품군/타입)',
   customer_tradingview_id      VARCHAR(255) NULL COMMENT '트레이딩뷰 아이디',
   customer_telegram_nickname   VARCHAR(255) NULL COMMENT '텔레그램 닉네임',
   customer_free_room           TINYINT(1)   NULL COMMENT '무료방 가입 여부(0/1)',
   customer_signal_room         TINYINT(1)   NULL COMMENT '시그널방 가입 여부(0/1)',
   customer_exchange_joined     TINYINT(1)   NULL COMMENT '거래소 가입 여부(0/1)',
   customer_tradingview_joined  TINYINT(1)   NULL COMMENT '트레이딩뷰 가입 여부(0/1)',
   customer_indicator_flag      TINYINT(1)   NULL COMMENT '지표 유무(0/1)',
   for_log_created_at           DATETIME     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '업데이트 로그를 위한 생성 시각',
   CONSTRAINT fk_cust_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='유효/최초 고객';

CREATE INDEX idx_cust_phone_created   ON customers(customer_phone, customer_created_at DESC);

CREATE INDEX ix_c_created_key
    ON customers (customer_created_at DESC, customer_id DESC);
CREATE INDEX ix_c_user_created_key
    ON customers (user_id, customer_created_at DESC, customer_id DESC);
CREATE INDEX ix_c_status_filter_recent
    ON customers (customer_status, customer_created_at DESC, customer_id DESC);
CREATE INDEX ix_c_division_filter_recent
    ON customers (customer_division, customer_created_at DESC, customer_id DESC);
CREATE INDEX ix_c_status_promise_created_id
    ON customers (customer_status, customer_promise_time, customer_created_at DESC, customer_id DESC);

-- ========================================
-- 중복 DB (중복)
-- ========================================

CREATE TABLE customers_duplicate (
     duplicate_id                 BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '중복DB PK',
     customer_id                  BIGINT NOT NULL COMMENT '연관 고객 ID',
     duplicate_created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '중복 등록 시각',
     duplicate_name               VARCHAR(255)  NOT NULL COMMENT '표시용 이름',
     duplicate_memo               TEXT NULL COMMENT '메모',
     duplicate_content            TEXT NULL COMMENT '내용',
     duplicate_source             VARCHAR(255) NULL COMMENT 'DB 출처',
     duplicate_category           VARCHAR(50)  NOT NULL COMMENT '카테고리',
     duplicate_display            TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '메뉴 표시 여부(1=표시)',
     CONSTRAINT fk_dup_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='중복 전용 테이블(종류는 customers에서 유추)';

CREATE INDEX idx_dup_customer          ON customers_duplicate(customer_id);

CREATE INDEX ix_dup_display_created_id   ON customers_duplicate (duplicate_display, duplicate_created_at DESC, duplicate_id DESC);
CREATE INDEX idx_dup_created_id
    ON customers_duplicate (duplicate_created_at, duplicate_id);

CREATE INDEX ix_dup_customer_created
    ON customers_duplicate (customer_id, duplicate_created_at);

CREATE INDEX ix_dup_created_customer
    ON customers_duplicate (duplicate_created_at, customer_id);

-- ========================================
-- 과거담당자 이력
-- ========================================

CREATE TABLE customer_past_users (
     past_user_id  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '이력 PK',
     customer_phone VARCHAR(20) NOT NULL COMMENT '고객 전화번호',
     user_id      BIGINT NOT NULL COMMENT '과거 담당자 ID',
     CONSTRAINT fk_cpu_user     FOREIGN KEY (user_id)    REFERENCES users(user_id)     ON DELETE CASCADE,
     CONSTRAINT uk_cpu_phone_user UNIQUE (customer_phone, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='과거 담당자 이력(단순 매핑)';

-- 조회 최적화 인덱스
CREATE INDEX idx_cpu_customer ON customer_past_users(customer_phone);
CREATE INDEX idx_cpu_user     ON customer_past_users(user_id);

-- ========================================
-- 구글 스프레드 시트
-- ========================================
CREATE TABLE gsheet_sources (
    source_id       BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',
    spreadsheet_id  VARCHAR(80)  NOT NULL COMMENT '구글 스프레드시트 ID',
    sheet_name      VARCHAR(100) NOT NULL COMMENT '탭(시트) 이름',
    cursor_row      INT          NOT NULL DEFAULT 1 COMMENT '마지막 처리한 행 번호(헤더=1 기준)',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '마지막 동기화 시각',
    UNIQUE KEY uq_gsheet (spreadsheet_id, sheet_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO gsheet_sources (
    source_id, spreadsheet_id, sheet_name, cursor_row, updated_at
) VALUES
    (1, '~~~', 'Sheet1', 1, now());
```
