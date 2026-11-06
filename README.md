# HexagonGMI 인사관리 시스템 (HRMS)

> 직무교육 졸업 프로젝트 · 팀 **HexagonGMI**
> 사원 · 조직 · 근태 · 휴가 · 권한을 한곳에서 관리하는 사내 인사관리 웹 서비스

- **배포**: AWS EC2 (Ubuntu) · Tomcat 9 · `http://[EC2 퍼블릭 IP]:8888/`
- **발표**: 2026. 11. 06

<br>

## 목차

1. [프로젝트 소개](#1-프로젝트-소개)
2. [기술 스택](#2-기술-스택)
3. [Frontend – 동적 UI 및 비동기 통신 구현](#3-frontend--동적-ui-및-비동기-통신-구현)
4. [Backend](#4-backend)
5. [협업 – GitHub 브랜치 전략 · Flyway](#5-협업--github-브랜치-전략--flyway)
6. [Database](#6-database)
7. [클라우드 – AWS 기반 서비스 배포 환경 구축](#7-클라우드--aws-기반-서비스-배포-환경-구축)
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
| 하재훈 | 총괄 · 전체 구조 / DB 설계 · 인사 · AWS 배포 |
| 김상호 | 권한 (권한 목록 · 권한 그룹 · 계정 권한 부여) |
| 이수빈 | 근태 (출퇴근 · 근태 현황 · 정정 결재) |
| 양원경 | 휴가 (휴가 신청 · 결재 · 잔여 연차 · 캘린더) |
| 곽규성 | 부서 (조직 트리 · 조직장 · 부서 관리) |

<br>

## 2. 기술 스택

| 구분 | 사용 기술 |
|---|---|
| Frontend | HTML · CSS · **JavaScript (ES6) · jQuery 3.7 · Fetch API** · Bootstrap 5 · AdminLTE 4 · FullCalendar 6 |
| Backend | Java 11 · Spring MVC 5.3 (Java Config) · **Spring Security 5.8** · **JSP · JSTL** (서버에서 실행되는 서블릿) · Lombok · Logback |
| Data | **MySQL 8** · **MyBatis** (mybatis-spring) · HikariCP · **Flyway 9** |
| Build · Server | **Maven** (WAR) · Apache Tomcat 9 |
| Cloud | **AWS EC2** (t3.micro) · Ubuntu Linux |
| 협업 | GitHub (main / develop / feature/*) · Pull Request · Figma |

<br>

## 3. Frontend – 동적 UI 및 비동기 통신 구현

- **Vanilla JavaScript 기반 웹 개발**: JavaScript와 DOM API를 활용한 동적 화면 구성 및 사용자 이벤트 처리
- **jQuery 활용**: DOM 탐색, 이벤트 처리 및 반복 코드 간소화를 통한 개발 생산성 향상
- **비동기 통신 구현**: Fetch API와 `async/await`를 활용한 서버 API 호출 및 비동기 데이터 처리
- **JSON 기반 동적 렌더링**: 서버에서 수신한 JSON 데이터를 활용한 화면 갱신 및 사용자 인터페이스 구현
- **Bootstrap 기반 반응형 UI**: 다양한 화면 크기를 고려한 반응형 웹 인터페이스 구현

> **핵심 정리**
> JavaScript 기반의 동적 화면 처리와 비동기 통신을 활용하여 사용자 경험을 개선하고,
> Bootstrap을 적용해 다양한 화면 환경에서도 이용할 수 있는 HR 관리 인터페이스를 구현했습니다.

```javascript
// 휴가 결재: DOM 탐색 · 이벤트는 jQuery, 서버 통신은 fetch + async/await
$(document).on("click", ".bt-approve", function () {   // 목록의 모든 승인 버튼을 이벤트 위임으로 처리
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
// 휴가 신청: 날짜를 고르면 사용 일수 · 잔여 연차를 서버에서 받아 바로 갱신
async function loadPlan() {
  const response = await fetch(ctx + "/api/leaves/plan?" + params);
  const result = await response.json();
  if (response.ok) setSummary(result.data);      // JSON → DOM 렌더링
  else showError(result.message);
}
dateEl.addEventListener("change", loadPlan);
```

<br>

## 4. Backend

### 4-1. 계층형 아키텍처

**Controller – Service – Mapper 계층 분리를 통한 관심사 분리**

```
브라우저 ──▶ Spring Security ──▶ Controller / RestController ──▶ Service ──▶ Mapper ──▶ MySQL
 (JSP·fetch)   (인증 · 권한 검사)    (JSP 화면 / JSON 응답)      (업무 규칙 ·     (인터페이스
                                                                  @Transactional)   + XML SQL)
```

| 패키지 | 역할 |
|---|---|
| `controller` | 화면 요청 → JSP 반환 |
| `rest.controller` | 버튼 동작 → JSON(`ApiResponse`) 반환 |
| `service` | 결재자 지정 · 연차 차감 · 근태 판정 등 **업무 규칙과 트랜잭션** |
| `mapper` + `resources/mappers/*.xml` | SQL 전담, MyBatis가 결과를 객체로 매핑 |
| `exception` | `ErrorCode` · `BusinessException` · 전역 예외 처리 |
| `security` | 인증 정보 · 권한 코드 · `SecurityConfig` |

### 4-2. 인증 및 세밀한 권한 관리

**Spring Security 기반 인증 · 인가 체계 구축**

- **RBAC(Role-Based Access Control)** 기반 역할별 접근 제어
- **Role과 Permission의 분리 설계**를 통한 세분화된 기능 접근 권한 관리
- 데이터베이스에 정의된 역할 및 권한 정보를 Spring Security의 **GrantedAuthority와 연동**
- 사용자 역할과 권한에 따른 **API 및 업무 기능 접근 제어**

> **핵심 정리**
> 인사관리 시스템의 핵심 요구사항인 **개인정보 보호와 역할별 접근 제어**를 위해 Spring Security를 도입했습니다.
> 관리자, 팀장, 일반 사원 등의 역할과 개별 업무 권한을 분리하여 유연한 권한 관리 체계를 구현했습니다.

```java
// DB의 Role → Permission 을 GrantedAuthority 로 변환
for (HrRole role : account.getRoles()) {
    codes.add(role.getRoleCode());                    // ROLE_*
    for (Permission permission : role.getPermissions())
        codes.add(permission.getPermissionCode());    // PERMISSION_*
}

// SecurityConfig: URL · HTTP 메서드별로 필요한 권한 검사
.requestMatchers(antMatcher("/employees/new")).hasAuthority(PermissionCode.EMP_CREATE)
.requestMatchers(antMatcher(HttpMethod.PUT, "/api/roles/**")).hasAuthority(PermissionCode.ROLE_UPDATE)
```

| 권한 그룹 (Role) | 대상 |
|---|---|
| 일반 사원 `ROLE_EMPLOYEE` | 모든 사원 (내 정보 · 내 근태 · 휴가 신청) |
| 팀 업무 승인자 `ROLE_TEAM_APPROVER` | 팀장 (팀 근태 · 휴가/정정 결재) |
| 부서 업무 승인자 `ROLE_DEPT_APPROVER` | 부서장 |
| 인사 담당자 `ROLE_HR_STAFF` | 인사팀 실무 |
| 인사 관리자 `ROLE_HR_ADMIN` | 인사팀장 (사원 등록 · 부서 관리 · 전사 현황) |
| 시스템 관리자 `ROLE_SYSTEM_ADMIN` | 계정 · 권한 관리 |

- 계정 보호: BCrypt 비밀번호 · 5회 실패 시 잠금 · 권한 상승 방지(자신에게 없는 권한은 부여 불가)

### 4-3. 트랜잭션 및 예외 처리 표준화

**트랜잭션**

- Spring의 `@Transactional`을 활용한 **서비스 계층의 트랜잭션 관리**
- 다중 데이터 변경 작업에 대한 **데이터 정합성 및 일관성 확보**
- 업무 처리 실패 시 **트랜잭션 롤백**을 통한 데이터 무결성 유지

| 업무 | 한 트랜잭션으로 묶은 작업 |
|---|---|
| 휴가 승인 | 신청 상태 변경 + 연차 사용 일수 증가 (차감 실패 시 상태 변경도 롤백) |
| 사원 등록 | 사원 + 계정 + 기본 권한 + 올해 연차 + 입사 발령 이력 |

**예외 처리 아키텍처**

- `ErrorCode`를 활용한 **예외 코드 및 메시지 표준화**
- `BusinessException`을 통한 업무 규칙 위반 및 비즈니스 예외 처리
- `@ControllerAdvice`, `@RestControllerAdvice` 기반 **전역 예외 처리 체계** 구축
- 일관된 오류 응답 구조를 통한 유지보수성 향상

| ErrorCode | HTTP | 사용 예 |
|---|---|---|
| `INVALID_INPUT` | 400 | 필수 값 누락 · 형식 오류 |
| `BUSINESS_RULE` | 400 | 잔여 연차 부족 등 업무 규칙 위반 |
| `FORBIDDEN` | 403 | 권한 없는 대상 처리 |
| `NOT_FOUND` | 404 | 없는 사원 · 부서 · 계정 |
| `CONFLICT` | 409 | 이미 다른 사람이 처리한 신청 |
| `INTERNAL_ERROR` | 500 | 처리하지 못한 예외 |

> **핵심 정리**
> 서비스 계층에서는 트랜잭션을 통해 데이터 정합성을 확보하고, 공통 예외 처리 아키텍처를 도입하여
> 오류 코드와 메시지를 표준화했습니다. 이를 통해 중복 코드를 줄이고 유지보수성을 향상시켰습니다.

### 4-4. REST API 및 MyBatis 데이터 접근 계층

**REST API**

- REST 설계 원칙을 고려한 **HTTP 기반 API** 구현
- **GET, POST, PUT, DELETE**를 활용한 자원 중심 API 설계
- `ApiResponse<T>` 기반의 **공통 JSON 응답 구조** 표준화
- 프론트엔드와 백엔드의 역할 분리 및 비동기 통신 구조 구현

| Method | URL | 설명 |
|---|---|---|
| GET | `/api/leaves/plan` | 휴가 사용 일수 · 잔여 연차 미리보기 |
| POST | `/api/leaves` | 휴가 신청 |
| POST | `/api/leaves/{id}/approve` · `/reject` | 휴가 승인 · 반려 |
| POST | `/api/attendance/check-in` · `/check-out` | 출근 · 퇴근 |
| PUT | `/api/roles/{id}` | 권한 그룹 수정 |
| DELETE | `/api/departments/{id}` | 부서 삭제 |
| DELETE | `/api/accounts/{id}/roles/{roleId}` | 계정 권한 회수 |

```json
// 성공
{ "result": "success", "code": "OK",
  "message": "L-0012 휴가를 신청했습니다.",
  "data": { "leaveRequestId": 12, "status": "REQUESTED" } }

// 실패 (409)
{ "result": "fail", "code": "CONFLICT",
  "message": "방금 다른 처리가 되어 승인할 수 없습니다." }
```

**SQL Mapper (MyBatis)**

- **MyBatis – Spring 연동**: `SqlSessionFactory` · `@MapperScan` 설정, Mapper 인터페이스 + XML
- **`association`을 이용한 1:1 관계 처리**: 사원 → 부서 · 직급, 휴가 신청 → 신청자 · 결재자
- **`collection`을 이용한 1:多 관계 처리**: 권한 그룹 → 권한 목록, 사원 → 인사 이력

```xml
<resultMap id="hrRoleMap" type="HrRole">
  <id column="hr_role_id" property="hrRoleId"/>
  <result column="role_name" property="roleName"/>
  <!-- 1:多 권한 그룹 → 권한 목록 -->
  <collection property="permissions" ofType="Permission" column="hr_role_id"
              select="com.hexagon.hrms.mapper.PermissionMapper.selectByHrRoleId"/>
</resultMap>
```

> **핵심 정리**
> REST API를 통해 프론트엔드와 백엔드 간 통신 구조를 표준화하고,
> MyBatis의 객체 관계 매핑 기능을 활용해 HR 시스템의 복합적인 데이터 관계를 처리했습니다.

<br>

## 5. 협업 – GitHub 브랜치 전략 · Flyway

**GitHub 브랜치 전략**

- GitHub를 활용한 소스 코드 형상 관리
- `main`, `develop`, `feature/*` 브랜치를 구분한 협업 전략 적용
- **Pull Request** 기반 코드 통합 및 변경 이력 관리
- 기능 단위 브랜치 분리로 팀원 간 개발 충돌 최소화

```
main (배포) ◀── develop (통합) ◀── feature/emp · feature/leave · feature/attendance ...
```

**Flyway – 팀원 간 SQL 버전 관리**

- SQL 마이그레이션 파일을 활용한 **데이터베이스 스키마 버전 관리**
- 팀원 간 데이터베이스 구조 및 변경 이력 공유
- 버전별 스키마 변경 사항을 관리하여 **개발 환경의 일관성 확보**

```
src/main/resources/db/migration/
  V1__hr_init.sql   -- 테이블 · FK · 제약조건
  V2__hr_seed.sql   -- 권한 · 직급 · 휴가 유형 · 목 데이터
```

> **핵심 정리**
> GitHub 브랜치 전략과 Pull Request를 통해 체계적으로 소스 코드를 통합했습니다.
> 또한 Flyway를 도입하여 소스 코드뿐 아니라 **데이터베이스 스키마까지 버전 관리**함으로써 협업 환경의 일관성을 확보했습니다.

<br>

## 6. Database

**관계형 데이터베이스 설계 및 SQL 활용**

- **MySQL 8.0** 및 Oracle Database 기반 관계형 데이터베이스 활용
- 업무 요구사항 분석을 통한 **ERD 설계** 및 테이블 관계 정의
- **데이터 정규화**를 통한 중복 최소화 및 데이터 무결성 확보
- **DDL · DML · DCL**을 활용한 데이터베이스 객체, 데이터 및 권한 관리
- **JOIN 및 서브쿼리**를 활용한 복합 업무 데이터 조회
- 데이터베이스 **사용자 계정 생성 및 접근 권한 설정**

| 영역 | 테이블 (16개) |
|---|---|
| 조직 · 인사 | `department` (상위 부서 트리 · 조직장) · `job` · `emp` · `hr_appointment` |
| 계정 · 권한 | `account` · `hr_role` · `permission` · `hr_role_permission` · `account_role` · `account_role_history` |
| 근태 · 휴가 | `attendance` · `attendance_correction` · `leave_type` · `annual_leave` · `leave_request` · `holiday` |

```sql
-- DCL: root 대신 애플리케이션 전용 계정에 hrms DB 권한만 부여
CREATE USER 'hrms'@'localhost' IDENTIFIED BY '********';
GRANT ALL PRIVILEGES ON hrms.* TO 'hrms'@'localhost';

-- 서브쿼리: 오늘 출근 기록이 없는 재직자 (결근 후보)
SELECT e.emp_id, e.ename, d.department_name
  FROM emp e
  JOIN department d ON d.department_id = e.department_id
 WHERE e.status = 'ACTIVE'
   AND NOT EXISTS (SELECT 1 FROM attendance a
                    WHERE a.emp_id = e.emp_id AND a.work_date = CURDATE());
```

> **핵심 정리**
> HR 시스템의 사원, 부서, 직급, 역할 및 권한 등 다양한 엔터티 간 관계를 분석하여
> 관계형 데이터베이스를 설계하고, SQL을 활용하여 업무 데이터를 관리했습니다.

<br>

## 7. 클라우드 – AWS 기반 서비스 배포 환경 구축

- **AWS EC2** 기반 클라우드 서버 환경 구축
- **Ubuntu Linux** 기반 서버 운영 환경 구성
- **Apache Tomcat**을 활용한 Spring MVC WAR 애플리케이션 배포
- **Maven**을 활용한 프로젝트 빌드 및 배포 산출물 관리
- **EC2 보안 그룹** 및 네트워크 접근 제어 설정

> 단, 제한된 서버 자원(**EC2 t3.micro, RAM 1GiB**)을 고려하여, Oracle이 아닌 **MySQL 8.0** 데이터베이스로 구성했습니다.

```
[브라우저] ──▶ [보안 그룹: 22(SSH) / 8888(HTTP)]
                └▶ EC2 t3.micro · Ubuntu (RAM 1GiB + Swap 2GiB)
                     ├─ Tomcat 9  ── ROOT.war (Spring MVC + Flyway)
                     └─ MySQL 8 (localhost, 앱 전용 계정)
```

**배포 과정**

```bash
git pull                                        # 최신 코드
mvn clean package -DskipTests                   # target/hrms.war 생성
sudo /opt/tomcat9/bin/shutdown.sh
sudo rm -rf /opt/tomcat9/webapps/ROOT /opt/tomcat9/webapps/ROOT.war
sudo cp target/hrms.war /opt/tomcat9/webapps/ROOT.war
sudo /opt/tomcat9/bin/startup.sh                # 시작 시 Flyway가 테이블 · 데이터 자동 생성
```

> **핵심 정리**
> AWS EC2의 Ubuntu Linux 환경에 애플리케이션 서버와 데이터베이스를 구축하고,
> Maven으로 빌드한 WAR 파일을 Tomcat에 배포하는 과정을 경험했습니다. 특히 제한된 서버 자원을 고려하여 배포 환경을 구성했습니다.

<br>

## 8. 주요 기능

### 인사 · 부서
- 사원 등록 · 수정 · 퇴직, **사번 자동 생성** (입사연도 + 순번 4자리)
- 부서 · 직급 변경 시 **인사 이력 자동 기록** (승진 / 전보)
- 본사 › 부 › 팀 **3단계 조직 트리**, 조직장 지정
- 권한에 따른 **조회 범위** (전사 / 부서 / 팀)

### 권한
- 권한 목록 · 권한 그룹 관리 · 계정 권한 부여 / 회수 (사유 · 이력 기록)
- 계정 관리: 잠금 해제 · 비밀번호 초기화 · 사용 / 중지

### 근태
- 출퇴근 기록, 지각 · 조퇴 · **결근 자동 판정** (주말 · 공휴일 · 휴가 제외)
- 근무 시간 · 초과 근무 계산, 근태 현황 CSV 다운로드
- **정정 신청 → 조직장 결재 → 재판정**

### 휴가
- 연차 · 반차 · 병가 · 경조사 · 공가
- **결재자 자동 지정** (내 부서 조직장 → 조직장 본인이면 상위 부서 조직장)
- 신청 전 **잔여 연차 미리보기**, 승인 시 연차 차감
- 근속연수에 따른 연차 자동 부여, 휴가 캘린더(FullCalendar)

<br>

## 9. 실행 방법 · 시연 계정

### 로컬 실행

1. MySQL에 `hrms` 데이터베이스 생성 (`utf8mb4`)
2. `src/main/resources/db.properties`에 접속 정보 작성 (**비밀번호는 커밋하지 않음**)
3. STS에서 Tomcat 9로 실행 → Flyway가 테이블 · 데이터 자동 생성
4. `http://localhost:8080/hrms` 접속

### 시연 계정 (비밀번호 `1234`)

| 아이디 | 이름 | 역할 |
|---|---|---|
| `user001` | 조용훈 | 개발1팀 사원 (일반 사원) |
| `user002` | 박유경 | 개발1팀장 (팀 업무 승인자) |
| `user003` | 윤현석 | 인사팀장 (인사 관리자) |
| `user004` | 이주철 | 이사 · 시스템 관리자 (전체 권한) |

<br>

## 10. 트러블슈팅 · 회고

| 문제 | 원인 | 해결 |
|---|---|---|
| 권한 그룹 API가 열려 있음 | 보안 규칙 누락 | `SecurityConfig`에 메서드별 권한 규칙 + 권한 상승 방지 |
| API마다 다른 응답 모양 | 컨트롤러마다 응답을 따로 구성 | `ApiResponse<T>` · `ErrorCode`로 통일 |
| Flyway checksum 오류 | 이미 적용된 마이그레이션 파일 수정 | 적용된 파일은 수정하지 않는 규칙 정립, 개발 DB 재생성 |
| EC2에서 Maven 빌드 중 서버 멈춤 | t3.micro 메모리 1GiB 부족 | Swap 2GiB 추가, 빌드 중 Tomcat 중지 |
| AWS 배포 후 앱 시작 실패 | `mybatis-config.xml`이 `src/main/java`에 있어 Maven이 WAR에 포함하지 않음 (STS에서는 정상) | `src/main/resources`로 이동 → IDE와 Maven 빌드 차이 이해 |

**잘한 점** – 권한 · 결재 · 근태 규칙을 실제 회사처럼 설계 / Flyway · PR로 팀 환경 통일 / AWS 배포까지 경험
**아쉬운 점** – 권한 변경은 재로그인 후 반영 / 알림 기능 없음 / 테스트 코드 부족
**개선 방향** – 세션 권한 즉시 갱신 · 다단계 결재와 알림 · 테스트 코드와 CI/CD 자동 배포
