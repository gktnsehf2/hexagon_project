/* =====================================================================
   대시보드 시연용 "오늘" 데이터 (MySQL)

   왜 필요한가
     V2 목 데이터의 출퇴근 기록은 2026-10-26 ~ 10-30 까지만 있어서
     그 뒤 날짜에 대시보드를 열면 오늘 출근·지각·휴가가 모두 0 으로 나온다.
     이 스크립트는 실행한 날(CURDATE) 기준으로 오늘 현황을 만들어 준다.

   사용법
     시연 전에 MySQL Workbench 에서 hrms DB 를 선택하고 전체 실행 (Ctrl + Shift + Enter)
     여러 번 실행해도 결과가 같다. 다음 날 다시 실행하면 그날 데이터가 생긴다.
     Flyway 마이그레이션(V1, V2 ...)이 아니므로 src/main/resources/db/migration 에 넣지 않는다.

   만드는 데이터
     1) 오늘 출근 기록: 재직자 대부분 정상 출근, 일부 지각(09:01 이후), 일부는 기록 없음(결근)
        시연 · 테스트 계정(1, 5, 7, 8, 23, 48, 49, 54, 105)은 직접 [출근] 버튼을 눌러 보도록 비워 둔다
     2) 오늘 휴가 4건 (승인 완료): 개발1팀 연차·오전 반차, 재무회계팀 연차, 영업1팀 병가
     3) 개발1팀장 박유경(user002) 결재 대기: 휴가 1건 + 근태 정정 1건
        (V2 에 있는 개발1팀 대기 휴가 2건 + 근태 정정 1건과 합쳐 5건)

   지울 때 기준: reason 이 '[데모]' 로 시작하는 신청
     - 지난 날짜에 승인된 데모 휴가는 기록으로 남긴다
     - 오늘·앞으로의 데모 휴가와 결재 전인 데모 신청은 지우고 다시 만든다
   ===================================================================== */

SET @old_safe_updates := @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0; -- Workbench 기본 설정(키 없는 UPDATE/DELETE 차단) 잠시 해제

START TRANSACTION;

-- ---------------------------------------------------------------------
-- 1. 이전에 만든 데모 신청 정리
-- ---------------------------------------------------------------------

-- 1-1) 지울 승인 휴가의 연차 사용 일수를 먼저 되돌림
UPDATE annual_leave al
JOIN (SELECT lr.emp_id, YEAR(lr.start_date) AS leave_year, SUM(lr.days) AS days
        FROM leave_request lr
        JOIN leave_type lt ON lt.leave_type_id = lr.leave_type_id
       WHERE lr.reason LIKE '[데모]%'
         AND lr.status = 'APPROVED'
         AND lt.is_deduct = 1
         AND lr.start_date >= CURDATE()
       GROUP BY lr.emp_id, YEAR(lr.start_date)) d
  ON d.emp_id = al.emp_id AND d.leave_year = al.leave_year
SET al.used_days = al.used_days - d.days;

-- 1-2) 데모 신청 삭제
DELETE FROM leave_request
 WHERE reason LIKE '[데모]%'
   AND (status <> 'APPROVED' OR start_date >= CURDATE());

DELETE FROM attendance_correction
 WHERE reason LIKE '[데모]%'
   AND status = 'REQUESTED';

-- ---------------------------------------------------------------------
-- 2. 오늘 휴가 (승인 완료, 1단계 결재: 소속 팀장)
-- ---------------------------------------------------------------------

-- 개발1팀 최민준 연차 (결재자 개발1팀장 박유경 48)
INSERT INTO leave_request (emp_id, leave_type_id, start_date, end_date, days, reason, status, request_date, approver_id, approved_at)
VALUES (52, 1, CURDATE(), CURDATE(), 1.0, '[데모] 개인 사유', 'APPROVED', TIMESTAMP(CURDATE() - INTERVAL 7 DAY, '10:12:00'), 48, TIMESTAMP(CURDATE() - INTERVAL 7 DAY, '13:40:00'));

-- 개발1팀 전진우 오전 반차 (14:00 출근)
INSERT INTO leave_request (emp_id, leave_type_id, start_date, end_date, days, reason, status, request_date, approver_id, approved_at)
VALUES (53, 2, CURDATE(), CURDATE(), 0.5, '[데모] 병원 진료', 'APPROVED', TIMESTAMP(CURDATE() - INTERVAL 3 DAY, '17:05:00'), 48, TIMESTAMP(CURDATE() - INTERVAL 2 DAY, '09:15:00'));

-- 재무회계팀 연차 (결재자 재무회계팀장 15)
INSERT INTO leave_request (emp_id, leave_type_id, start_date, end_date, days, reason, status, request_date, approver_id, approved_at)
VALUES (20, 1, CURDATE(), CURDATE(), 1.0, '[데모] 가족 행사', 'APPROVED', TIMESTAMP(CURDATE() - INTERVAL 10 DAY, '14:30:00'), 15, TIMESTAMP(CURDATE() - INTERVAL 9 DAY, '10:00:00'));

-- 영업1팀 임우진 병가 (연차 차감 없음, 결재자 영업1팀장 28)
INSERT INTO leave_request (emp_id, leave_type_id, start_date, end_date, days, reason, status, request_date, approver_id, approved_at)
VALUES (31, 4, CURDATE(), CURDATE(), 1.0, '[데모] 감기 몸살', 'APPROVED', TIMESTAMP(CURDATE(), '07:50:00'), 28, TIMESTAMP(CURDATE(), '08:20:00'));

-- 승인된 연차·반차만큼 사용 일수 증가 (1-1 과 같은 조건)
UPDATE annual_leave al
JOIN (SELECT lr.emp_id, YEAR(lr.start_date) AS leave_year, SUM(lr.days) AS days
        FROM leave_request lr
        JOIN leave_type lt ON lt.leave_type_id = lr.leave_type_id
       WHERE lr.reason LIKE '[데모]%'
         AND lr.status = 'APPROVED'
         AND lt.is_deduct = 1
         AND lr.start_date >= CURDATE()
       GROUP BY lr.emp_id, YEAR(lr.start_date)) d
  ON d.emp_id = al.emp_id AND d.leave_year = al.leave_year
SET al.used_days = al.used_days + d.days;

-- ---------------------------------------------------------------------
-- 3. 오늘 출근 기록 (이미 기록이 있는 사람은 건드리지 않음)
-- ---------------------------------------------------------------------

-- 3-1) 하루 휴가자는 ON_LEAVE (V2 목 데이터와 같은 방식, 반차는 출근해야 하므로 제외)
INSERT INTO attendance (emp_id, work_date, status)
SELECT DISTINCT lr.emp_id, CURDATE(), 'ON_LEAVE'
  FROM leave_request lr
  JOIN leave_type lt ON lt.leave_type_id = lr.leave_type_id
 WHERE lr.status = 'APPROVED'
   AND CURDATE() BETWEEN lr.start_date AND lr.end_date
   AND lt.leave_type_code NOT IN ('HALF_AM', 'HALF_PM')
   AND NOT EXISTS (SELECT 1 FROM attendance a WHERE a.emp_id = lr.emp_id AND a.work_date = CURDATE());

-- 3-2) 나머지 재직자 (사번으로 계산해서 매번 같은 결과)
--      MOD(emp_id * 47, 100) :  0~2 출근 안 함(결근) / 3~9 지각 09:01~09:35 / 나머지 정상 08:15~08:59
INSERT INTO attendance (emp_id, work_date, check_in_at, status)
SELECT e.emp_id,
       CURDATE(),
       CASE WHEN MOD(e.emp_id * 47, 100) < 10
            THEN TIMESTAMP(CURDATE(), '09:01:00') + INTERVAL MOD(e.emp_id, 35) MINUTE + INTERVAL MOD(e.emp_id * 13, 60) SECOND
            ELSE TIMESTAMP(CURDATE(), '08:15:00') + INTERVAL MOD(e.emp_id * 7, 45) MINUTE + INTERVAL MOD(e.emp_id * 13, 60) SECOND
       END,
       CASE WHEN MOD(e.emp_id * 47, 100) < 10 THEN 'LATE' ELSE 'NORMAL' END
  FROM emp e
 WHERE e.status = 'ACTIVE'
   AND e.hiredate <= CURDATE()
   AND e.emp_id NOT IN (1, 5, 7, 8, 23, 48, 49, 54, 105)  -- 시연 · 테스트 계정은 직접 출근
   AND MOD(e.emp_id * 47, 100) >= 3             -- 결근 (출근 기록 없음)
   AND NOT EXISTS (SELECT 1 FROM attendance a WHERE a.emp_id = e.emp_id AND a.work_date = CURDATE())
   AND NOT EXISTS (SELECT 1 FROM leave_request lr
                    WHERE lr.emp_id = e.emp_id
                      AND lr.status = 'APPROVED'
                      AND CURDATE() BETWEEN lr.start_date AND lr.end_date);

-- ---------------------------------------------------------------------
-- 4. 개발1팀장 박유경(48) 결재 대기
-- ---------------------------------------------------------------------

-- 손태윤 연차 2일 (2주 뒤) → 결재자 박유경 대기
INSERT INTO leave_request (emp_id, leave_type_id, start_date, end_date, days, reason, status, request_date, approver_id)
VALUES (50, 1, CURDATE() + INTERVAL 13 DAY, CURDATE() + INTERVAL 14 DAY, 2.0, '[데모] 가족 여행', 'REQUESTED',
        TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '16:20:00'), 48);

-- 개발1팀 51번 어제 출근 태깅 누락 → 근태 정정 신청
INSERT INTO attendance_correction (emp_id, work_date, request_check_in_at, request_check_out_at, reason, status, request_date, approver_id)
VALUES (51, CURDATE() - INTERVAL 1 DAY,
        TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '08:47:00'), TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '18:12:00'),
        '[데모] 출입 카드 미태깅', 'REQUESTED', TIMESTAMP(CURDATE(), '08:40:00'), 48);

COMMIT;

SET SQL_SAFE_UPDATES = @old_safe_updates;

-- ---------------------------------------------------------------------
-- 확인
-- ---------------------------------------------------------------------
SELECT CURDATE() AS 오늘,
       COUNT(CASE WHEN check_in_at IS NOT NULL THEN 1 END) AS 출근,
       COUNT(CASE WHEN status = 'LATE' THEN 1 END) AS 지각,
       COUNT(CASE WHEN status = 'ON_LEAVE' THEN 1 END) AS 휴가
  FROM attendance
 WHERE work_date = CURDATE();
