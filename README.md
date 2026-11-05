# HexagonGMI 인사관리 시스템 (HRMS)

> 직무교육 졸업 프로젝트 · 팀 **HexagonGMI**
> 사원 · 조직 · 근태 · 휴가 · 권한을 한곳에서 관리하는 사내 인사관리 웹 서비스

- **배포**: AWS EC2 (Ubuntu) · `http://[EC2 퍼블릭 IP]:8080/hrms`
- **발표**: 2026. 11. 06

<br>

## 목차

1. [프로젝트 소개](#1-프로젝트-소개)
2. [기술 스택](#2-기술-스택)
3. [Frontend – 동적 UI 및 비동기 통신](#3-frontend--동적-ui-및-비동기-통신)
4. [Backend](#4-backend)
5. [Database](#5-database)
6. [협업 – GitHub 브랜치 전략 · Flyway](#6-협업--github-브랜치-전략--flyway)
7. [클라우드 – AWS EC2 배포](#7-클라우드--aws-ec2-배포)
8. [주요 기능](#8-주요-기능)
9. [실행 방법 · 시연 계정](#9-실행-방법--시연-계정)
10. [트러블슈팅 · 회고](#10-트러블슈팅--회고)

<br>

## 1. 프로젝트 소개

작은 회사일수록 인사 업무를 엑셀과 메신저로 처리합니다. 그러면

- 사원 · 조직도 · 발령 기록이 **흩어져** 이력을 추적하기 어렵고
- 휴가 · 근태 정정 결재가 **누구에게 있는지 보이지 않고**
- 권한을 "관리자 / 일반"으로만 **뭉뚱그려** 주게 됩니다.

HexagonGMI는 인사 업무 5개 영역(**인사 · 부서 · 근태 · 휴가 · 권한**)을 하나의 시스템으로 묶고,
**로그인한 사람의 권한에 따라 메뉴와 조회 범위가 달라지도록** 만든 HRMS입니다.

### 팀 구성

| 이름 | 담당 |
|---|---|
| 하재훈 | 총괄 · 전체 구조 / DB 설계 · 인사 · 부서 · AWS 배포 |
| 김상호 | 권한 (권한 목록 · 권한 그룹 · 계정 권한 부여) |
| 이수빈 | 근태 (출퇴근 · 근태 현황 · 정정 결재) |
| 양원경 | 휴가 (휴가 신청 · 결재 · 잔여 연차 · 캘린더) |
| 곽규성 | 부서 (조직도 · 부서 등록)|

<br>

## 2. 기술 스택

| 구분 | 사용 기술 |
|---|---|
| Frontend | JSP · JSTL · **JavaScript (ES6) · jQuery 3.7 · Fetch API** · Bootstrap 5 · AdminLTE 4 · FullCalendar 6 |
| Backend | Java 11 · Spring MVC 5.3 (Java Config) · Spring Security 5.8 · Lombok · Logback |
| Data | **MySQL 8.0** · MyBatis 3.4 (mybatis-spring) · HikariCP · Flyway 9 |
| Build · Server | Maven (WAR) · Apache Tomcat 9 |
| Cloud | AWS EC2 (t3.micro) · Ubuntu Linux |
| 협업 | GitHub (main / develop / feature/*) · Pull Request · Figma · 엑셀 설계서 |

> **왜 Oracle이 아닌 MySQL인가?**
> 수업에서는 주로 Oracle을 배웠지만, 이 프로젝트는 **AWS EC2 t3.micro (RAM 1GiB)** 한 대에 Tomcat과 DB를 함께 올리는 것을 목표로 했습니다.
> 제한된 서버 자원에서 안정적으로 운영하기 위해 상대적으로 가벼운 **MySQL 8.0**을 선택했습니다.

<br>

## 3. Frontend – 동적 UI 및 비동기 통신

- **Vanilla JavaScript 기반 웹 개발**: 라이브러리 없이 JavaScript와 DOM API(`querySelector`, `addEventListener`)로 동적 화면 구성 및 사용자 이벤트 처리
- **jQuery 활용**: 공통 스크립트(`hrms.js`) · 대시보드 출퇴근 · 휴가 결재 화면에서 DOM 탐색 · 이벤트 위임으로 반복 코드 간소화
- **비동기 통신 구현**: Fetch API와 `async/await`로 승인 · 반려 · 출퇴근 · 미리보기 등을 페이지 이동 없이 서버 API 호출
- **JSON 기반 동적 렌더링**: 서버가 보낸 JSON(`ApiResponse.data`)으로 요약 · 표 · 알림 영역을 바로 갱신
- **Bootstrap 기반 반응형 UI**: Bootstrap 5 그리드 + AdminLTE 레이아웃, `hrms.css`의 `@media` 규칙으로 좁은 화면 대응

```javascript
// 휴가 결재: 화면 탐색 · 이벤트는 jQuery, 서버 통신은 fetch + async/await
$(document).on("click", ".bt-approve", function () {   // 목록의 모든 승인 버튼을 이벤트 위임으로
  fill($approveModal, $(this));
  bootstrap.Modal.getOrCreateInstance($approveModal[0]).show();
});

async function send($button, $modal, url, body) {
  $button.prop("disabled", true);
  const response = await fetch(url, { method: "POST", body: body });
  const result = await response.json();          // ApiResponse
  hrToast(result.message, response.ok ? "success" : "danger");
}
```

```javascript
// 휴가 신청 화면: 날짜를 고르면 사용 일수 · 잔여 연차를 미리 계산
async function loadPlan() {
  const response = await fetch(ctx + "/api/leaves/plan?" + params);
  const result = await response.json();          // ApiResponse
  if (response.ok) {
    setSummary(result.data);                     // JSON → DOM 갱신
  } else {
    showError(result.message);                   // 서버 오류 메시지
  }
}
dateEl.addEventListener("change", loadPlan);
```

> jQuery는 로컬 파일(`resources/js/jquery-3.7.1.min.js`)로 포함해 CDN 없이도 동작합니다.

<br>

## 4. Backend

### 4-1. 계층형 아키텍처

```
브라우저 ──▶ Spring Security ──▶ Controller / RestController ──▶ Service ──▶ Mapper ──▶ MySQL
 (JSP·fetch)   (인증 · URL별 권한)    (JSP 화면 / JSON 응답)      (업무 규칙 ·     (인터페이스
                                                                  @Transactional)   + XML SQL)
```

| 패키지 | 역할 |
|---|---|
| `controller` | 화면 요청 → JSP 반환 |
| `rest.controller` | 버튼 동작 → JSON(`ApiResponse`) 반환 |
| `service` | 결재자 지정 · 연차 차감 · 근태 판정 등 **업무 규칙과 트랜잭션** |
| `mapper` + `resources/mappers/*.xml` | SQL 전담, MyBatis가 결과를 객체로 매핑 |
| `domain` · `dto` | 테이블 객체 · 화면 전용 요청/응답 객체 |
| `exception` | `ErrorCode` · `BusinessException` · 전역 예외 처리 |
| `security` | 인증 정보(`AccountDetails`) · 권한 코드 · `SecurityConfig` |
| `common.web` | `ApiResponse<T>` · 페이징(`Pager`) |

### 4-2. 인증 및 세밀한 권한 관리 (Spring Security · RBAC)

- **RBAC(Role-Based Access Control)** 기반 역할별 접근 제어
- **Role과 Permission 분리**: 권한(Permission) 32개 = 기능 하나씩 → 권한 그룹(Role) 6개 = 업무 단위 묶음 → 계정에 부여
- **DB 권한 ↔ GrantedAuthority 연동**: 로그인 시 계정의 Role · Permission 코드를 `SimpleGrantedAuthority`로 변환
- **API · 화면 접근 제어**: 메뉴 숨김이 아니라 `SecurityConfig`에서 URL · HTTP 메서드별로 필요한 권한을 검사, 조회 범위(전사/부서/팀)는 Service에서 제한
- **계정 보호**: BCrypt 비밀번호 · 5회 실패 시 잠금 · 권한 상승 방지(자기 권한 변경 금지, 자신에게 없는 권한은 부여 불가)

```java
// AccountDetails: DB의 Role → Permission 을 GrantedAuthority 로
for (HrRole role : account.getRoles()) {
    codes.add(role.getRoleCode());                    // ROLE_*
    for (Permission permission : role.getPermissions())
        codes.add(permission.getPermissionCode());    // PERMISSION_*
}

// SecurityConfig: URL · 메서드별 필요한 권한
.requestMatchers(antMatcher("/employees/new")).hasAuthority(PermissionCode.EMP_CREATE)
.requestMatchers(antMatcher(HttpMethod.PUT, "/api/roles/**")).hasAuthority(PermissionCode.ROLE_UPDATE)
```

| 권한 그룹 (Role) | 권한 수 | 대상 |
|---|---|---|
| 일반 사원 `ROLE_EMPLOYEE` | 7 | 모든 사원 (내 정보 · 내 근태 · 휴가 신청) |
| 팀 업무 승인자 `ROLE_TEAM_APPROVER` | 4 | 팀장 (팀 근태 · 휴가/정정 결재) |
| 부서 업무 승인자 `ROLE_DEPT_APPROVER` | 4 | 부서장 |
| 인사 담당자 `ROLE_HR_STAFF` | 6 | 인사팀 실무 |
| 인사 관리자 `ROLE_HR_ADMIN` | 13 | 인사팀장 (사원 등록 · 부서 관리 · 전사 현황) |
| 시스템 관리자 `ROLE_SYSTEM_ADMIN` | 10 | 계정 · 권한 관리 |

> 권한 변경은 **다시 로그인한 뒤** 반영됩니다. (로그인 시점에 authorities를 만들기 때문)

### 4-3. 트랜잭션 및 예외 처리 표준화

**트랜잭션** – Service 계층의 `@Transactional`

- 휴가 승인 = `leave_request` 상태 변경 + `annual_leave` 사용 일수 증가 → 연차 차감이 실패하면 상태 변경도 **롤백**
- 사원 등록 = 사원 + 계정 + 기본 권한 그룹 + 올해 연차 + 입사 발령 이력을 **한 트랜잭션**으로 생성
- 승인 · 반려 · 취소는 `WHERE status = 'REQUESTED'` 조건부 UPDATE → 동시에 처리해도 한 번만 반영

**예외 처리 아키텍처**

- `ErrorCode`: 오류 코드 · HTTP 상태 · 기본 메시지 표준화
- `BusinessException(ErrorCode, message)`: 업무 규칙 위반을 서비스에서 던짐
- `@RestControllerAdvice`(`RestExceptionHandler`): API 오류를 `ApiResponse` 실패 응답으로 변환
- `@ControllerAdvice`(`GlobalExceptionHandler`): 화면 요청 오류를 에러 페이지(오류 코드 표시)로 변환

| ErrorCode | HTTP | 사용 예 |
|---|---|---|
| `INVALID_INPUT` | 400 | 필수 파라미터 누락 · 형식 오류 |
| `BUSINESS_RULE` | 400 | 잔여 연차 부족 · 반려 사유 없음 등 업무 규칙 위반 |
| `FORBIDDEN` | 403 | 권한 없는 대상 처리 |
| `NOT_FOUND` | 404 | 없는 사원 · 부서 · 계정 |
| `CONFLICT` | 409 | 방금 다른 사람이 승인/반려한 신청 |
| `INTERNAL_ERROR` | 500 | 처리하지 못한 예외 (로그 기록) |

### 4-4. REST API · ApiResponse&lt;T&gt;

화면의 버튼 동작은 `/api/**` REST API로 분리했습니다.

| Method | URL | 설명 |
|---|---|---|
| GET | `/api/leaves/plan` | 휴가 사용 일수 · 잔여 연차 미리보기 |
| POST | `/api/leaves` | 휴가 신청 |
| POST | `/api/leaves/{id}/approve` · `/reject` · `/cancel` | 휴가 승인 · 반려 · 취소 (상태 전이) |
| POST | `/api/attendance/check-in` · `/check-out` | 출근 · 퇴근 |
| POST | `/api/attendance/corrections` | 근태 정정 신청 |
| POST | `/api/employees/{id}/resign` | 퇴직 처리 |
| POST | `/api/roles` | 권한 그룹 등록 |
| PUT | `/api/roles/{id}` | 권한 그룹 수정 |
| PATCH | `/api/roles/{id}/deactivate` · `/activate` | 권한 그룹 사용 여부 |
| DELETE | `/api/departments/{id}` | 부서 삭제 (사용 기록이 있으면 '사용 안 함') |
| DELETE | `/api/accounts/{id}/roles/{roleId}` | 계정 권한 회수 |

모든 업무 API는 같은 구조로 응답합니다.

```json
// 성공 (200)
{ "result": "success", "code": "OK",
  "message": "L-0012 휴가를 신청했습니다. 결재자가 승인하면 확정됩니다.",
  "data": { "leaveRequestId": 12, "requestNo": "L-0012", "status": "REQUESTED" } }

// 실패 (409)
{ "result": "fail", "code": "CONFLICT",
  "message": "방금 다른 처리가 되어 승인할 수 없습니다. 새로고침 후 확인해 주세요." }
```

> 예외: 휴가 캘린더 API(`/api/calendar/events`)는 FullCalendar가 이벤트 **배열**을 그대로 받아야 해서 배열로 응답하고,
> 권한 관리 조회 API 일부는 목록을 그대로 반환합니다. 오류 응답은 모두 `ApiResponse` 형식입니다.

### 4-5. MyBatis 데이터 접근 계층

- **MyBatis – Spring 연동**: `RootConfig`에서 `SqlSessionFactory` · `@MapperScan` 설정, Mapper 인터페이스 + XML
- **association (1:1)**: 사원 → 부서 · 직급, 휴가 신청 → 신청자 · 결재자
- **collection (1:N)**: 권한 그룹 → 권한 목록, 사원 → 인사 이력 (10개 Mapper XML에서 34곳 사용)
- **동적 SQL**: `<if>` · `<where>` · `<foreach>`로 검색 조건과 조회 범위(전사/부서/팀) 조립

```xml
<resultMap id="hrRoleMap" type="HrRole">
  <id column="hr_role_id" property="hrRoleId"/>
  <result column="role_name" property="roleName"/>
  <!-- 1:N 권한 그룹 → 권한 목록 -->
  <collection property="permissions" ofType="Permission" column="hr_role_id"
              select="com.hexagon.hrms.mapper.PermissionMapper.selectByHrRoleId"/>
</resultMap>
```

<br>

## 5. Database

- MySQL 8.0 기반 관계형 데이터베이스, **16개 테이블**
- 업무 요구사항 분석 → ERD 설계 → 제3정규형 기준으로 중복 제거 (부서명 · 직급명은 FK로만 연결, N:M은 매핑 테이블로 분리)
- **DDL**: 테이블 · FK · 제약조건을 Flyway 스크립트(`V1`)로 관리
- **DML**: MyBatis SQL, 조건부 UPDATE로 동시 처리 방지
- **JOIN · 서브쿼리**: 사원 + 부서 + 직급 목록, 대시보드 집계, `NOT EXISTS`로 결근 판정
- **DCL · DB 사용자**: AWS 서버에서는 root 대신 애플리케이션 전용 계정을 만들고 `hrms` DB에 필요한 권한만 부여

| 영역 | 테이블 |
|---|---|
| 조직 · 인사 | `department` (parent_id 트리 · manager_id 조직장) · `job` · `emp` · `hr_appointment` |
| 계정 · 권한 | `account` · `hr_role` · `permission` · `hr_role_permission` · `account_role` · `account_role_history` |
| 근태 · 휴가 | `attendance` · `attendance_correction` · `leave_type` · `annual_leave` · `leave_request` · `holiday` |

```sql
-- 오늘 출근 기록이 없는 재직자 (결근 후보)
SELECT e.emp_id, e.ename, d.department_name
  FROM emp e
  JOIN department d ON d.department_id = e.department_id
 WHERE e.status = 'ACTIVE'
   AND NOT EXISTS (SELECT 1 FROM attendance a
                    WHERE a.emp_id = e.emp_id AND a.work_date = CURDATE());
```

<br>

## 6. 협업 – GitHub 브랜치 전략 · Flyway

- `main`(배포) ← `develop`(통합) ← `feature/*`(기능별) 브랜치 전략
- 기능 브랜치에서 작업 → **Pull Request**로 develop에 통합, 충돌은 PR 단계에서 해결
- **Flyway**로 DB 스키마도 버전 관리 → 팀원 PC와 AWS 서버 모두 서버 시작 시 같은 DB 구조 자동 생성

```
src/main/resources/db/migration/
  V1__hr_init.sql   -- 테이블 · FK
  V2__hr_seed.sql   -- 권한 · 권한 그룹 · 직급 · 휴가 유형 · 기본 데이터
```

> 서버가 시작될 때 Flyway가 아직 적용되지 않은 버전만 실행하고 `flyway_schema_history`에 기록합니다.
> DB 접속 정보(`db.properties`)는 각자 환경에 맞게 수정하고, 비밀번호가 담긴 변경은 커밋하지 않습니다.

<br>

## 7. 클라우드 – AWS EC2 배포

```
[브라우저] ──▶ [보안 그룹: 22(SSH, 내 IP) / 8080(HTTP)]
                └▶ EC2 t3.micro · Ubuntu (RAM 1GiB)
                     ├─ Tomcat 9  ── hrms.war (Spring MVC + Flyway)
                     └─ MySQL 8.0 (localhost)
```

1. **Maven 빌드**: `mvn clean package` → `target/hrms.war`
2. **EC2 · Ubuntu 준비**: JDK 11 · Tomcat 9 · MySQL 8 설치, `hrms` DB와 앱 전용 계정 생성
3. **보안 그룹**: SSH는 내 IP만, 서비스 포트만 외부 허용
4. **WAR 배포**: Tomcat `webapps`에 업로드 → 재시작 → Flyway가 테이블 · 기본 데이터 자동 생성

> 제한된 서버 자원(t3.micro, RAM 1GiB)을 고려해 Oracle 대신 MySQL 8.0으로 구성했습니다.

<br>

## 8. 주요 기능

### 인사 · 부서
- 사원 등록 · 수정 · 퇴직, **사번 자동 생성** (`입사연도 + 그해 순번 4자리`, 예: `20260001`)
- 부서 · 직급 변경 시 **인사 이력 자동 기록** (직급 상승 = 승진, 그 외 = 전보)
- 본사 › 부 › 팀 **3단계 조직 트리**, 조직장 지정, 사용 기록이 있으면 삭제 대신 '사용 안 함'
- 권한에 따른 **조회 범위** (전사 / 부서 / 팀)

### 권한
- 권한 목록 · 권한 그룹 관리 · 계정 권한 부여 · 계정 관리(잠금 해제 · 비밀번호 초기화 · 사용/중지)
- 부여 · 회수 이력과 사유 기록, 기본 그룹(일반 사원 · 시스템 관리자) 보호

### 근태
- 출퇴근 기록: 09:00 이후 출근 **지각**, 18:00 전 퇴근 **조퇴**
- **결근 자동 판정**: 평일 무기록 = 결근 (주말 · 공휴일 · 승인된 휴가 · 오늘 · 입사 전 제외)
- 근무 시간(점심 1시간 제외) · 18시 이후 30분 단위 초과 근무
- 근태 현황(팀/부서/전사) · CSV(엑셀) 다운로드, **정정 신청 → 조직장 결재 → 재판정**

### 휴가
- 연차 · 오전/오후 반차 · 병가 · 경조사 · 공가
- **결재자 자동 지정**: 내 부서 조직장 (내가 조직장이면 상위 부서 조직장, 최상위면 자동 승인)
- 신청 전 **잔여 연차 미리보기**, 승인 시 연차 차감 (부족하면 승인 불가)
- 연차 자동 부여: 첫해 `min(11, 12 - 입사월)`, 이후 `min(25, 15 + (근속연수 - 1) / 2)`
- 휴가 캘린더(FullCalendar) — 전 직원 휴가 · 공휴일

<br>

## 9. 실행 방법 · 시연 계정

### 로컬 실행

1. MySQL 8.0에 `hrms` 데이터베이스 생성
2. `src/main/resources/db.properties`에 접속 정보 작성
   ```properties
   jdbc.driver=com.mysql.cj.jdbc.Driver
   jdbc.url=jdbc:mysql://localhost:3306/hrms?serverTimezone=Asia/Seoul&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true
   jdbc.username=[계정]
   jdbc.password=[비밀번호]
   ```
3. STS(Eclipse)에서 Tomcat 9에 추가 후 실행 → Flyway가 테이블 · 데이터 자동 생성
4. `http://localhost:8080/hrms` 접속

### 시연 계정 (목 데이터 적용 시, 비밀번호 `1234`)

| 아이디 | 이름 | 역할 |
|---|---|---|
| `user049` | 정지현 | 개발1팀 사원 (일반 사원) |
| `user048` | 권진우 | 개발1팀장 (팀 업무 승인자) |
| `user007` | 고진우 | 인사팀장 (인사 관리자) |
| `user008` | 배윤서 | 인사팀 (인사 담당자) |
| `admin` | 관리자 | 전체 권한 그룹 |

<br>

## 10. 트러블슈팅 · 회고

| 문제 | 원인 | 해결 |
|---|---|---|
| 권한 그룹 API가 열려 있음 | 보안 규칙 누락으로 로그인만 하면 수정 가능 | `SecurityConfig`에 메서드별 권한 규칙 + 권한 상승 방지 |
| API마다 다른 응답 모양 | 컨트롤러마다 result · message를 따로 구성 | `ApiResponse<T>` · `ErrorCode`로 성공/실패 응답 통일 |
| 주석 정리 중 빌드 실패 | 주석과 함께 괄호 · 필드 · 메서드가 삭제됨 | `git diff`로 이전 커밋과 비교해 복구, 커밋 전 빌드 확인 |
| 지운 마이그레이션이 계속 실행 | 삭제한 V3가 Tomcat 배포 폴더에 남음 | 서버 Clean · DB 재생성 → 소스와 빌드 결과물 차이 이해 |

**잘한 점** – 권한 · 결재 · 근태 규칙을 실제 회사처럼 설계 / Flyway · PR로 팀 환경 통일 / AWS 배포까지 경험
**아쉬운 점** – 권한 변경은 재로그인 후 반영 / 알림 · 본인 비밀번호 변경 없음 / 테스트 코드 부족, 수업에서 다루지 않은 일부 기술은 AI 도움을 받아 구현
**개선 방향** – 세션 권한 즉시 갱신 · 다단계 결재와 메일 알림 · 테스트 코드와 CI/CD 자동 배포
