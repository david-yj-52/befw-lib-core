# CLAUDE.md — befw-lib-core JPA 공통화 개발 명세서

## 🧭 이 파일의 목적

Claude Code가 이 파일을 읽고 아래 3개 아이템을 **순서대로** 개발한다.
각 아이템은 이전 아이템에 의존하므로 반드시 순서를 지킨다.

```
아이템 3 (Tenant 격리 자동화)
        ↓
아이템 2 (에러 처리 공통화)
        ↓
아이템 1 (UK별 CRUD 자동 생성)
```

---

## 📁 프로젝트 구조

```
befw-lib-core/
└── src/main/java/com/tsh/starter/befw/lib/core/
    └── data/
        ├── constant/
        │   ├── MessagingSolutionType.java
        │   └── UseStatCd.java
        └── orm/
            ├── common/
            │   ├── access/
            │   │   ├── CrudService.java          ← 공통 인터페이스
            │   │   └── AbstractCrudService.java  ← 공통 추상 구현체
            │   ├── model/
            │   │   ├── BasicAudit.java           ← ID + Audit 필드
            │   │   └── BaseModel.java            ← tenant + useStatCd 등
            │   └── repo/
            │       └── BaseJpaRepository.java    ← 공통 쿼리
            └── gnMsgSrvConn/                     ← 엔티티 예시
                ├── GnMsgSrvConnAccess.java
                ├── GnMsgSrvConnModel.java
                └── GnMsgSrvConnRepo.java
```

### 기존 코드 규칙 (반드시 준수)

- 모든 엔티티는 `BaseModel`을 상속한다
- `BaseModel`은 `BasicAudit`을 상속한다
- 모든 Repository는 `BaseJpaRepository`를 상속한다
- 모든 Service는 `AbstractCrudService`를 상속한다
- Soft Delete: 실제 삭제 없이 `useStatCd = Delete`로 처리
- ID 전략: `@GeneratedValue(strategy = GenerationType.UUID)`
- 패키지: `com.tsh.starter.befw.lib.core.data.orm.common.*`

---

## ✅ 아이템 3. Tenant 격리 자동화

### 목적

개발자가 `tenant` 파라미터를 수동으로 넘기지 않아도 자동 주입되도록 한다.
실수로 tenant 조건을 빠뜨려 다른 테넌트 데이터가 노출되는 리스크를 방지한다.

### 생성할 파일 목록

```
data/orm/common/tenant/
├── TenantResolver.java               ← 인터페이스
├── ThreadLocalTenantResolver.java    ← 구현체
└── TenantContext.java                ← ThreadLocal 보관소

data/orm/common/exception/
└── TenantMissingException.java       ← Tenant 없을 때 예외
```

### 상세 명세

#### TenantContext.java
- `ThreadLocal<String>` 으로 tenant 값을 보관한다
- 메서드: `set(String tenant)`, `get()`, `clear()`
- `get()` 시 값이 null 또는 blank이면 `TenantMissingException`을 throw한다
- 클래스는 `final`로 선언하고 인스턴스화를 막는다 (private 생성자)

#### TenantResolver.java (인터페이스)
- 메서드: `void setTenant(String tenant)`, `String getTenant()`, `void clear()`
- 각 시스템 진입점(HTTP Filter, Solace 리스너, 배치 등)에서 이 인터페이스를 사용한다

#### ThreadLocalTenantResolver.java
- `TenantResolver` 구현체
- 내부적으로 `TenantContext`를 사용한다
- `@Component`로 등록한다

#### TenantMissingException.java
- `RuntimeException`을 상속한다
- 생성자: `TenantMissingException(String message)`
- 기본 메시지: `"Tenant is required but not set. Please set tenant before any DB access."`

#### BaseModel.java 수정
- Hibernate Filter를 적용하기 위해 아래 어노테이션을 추가한다
```java
@FilterDef(
    name = "tenantFilter",
    parameters = @ParamDef(name = "tenant", type = String.class)
)
@Filter(name = "tenantFilter", condition = "TENANT = :tenant")
```

#### AbstractCrudService.java 수정
- 모든 조회 메서드 실행 전 Hibernate Filter를 활성화한다
- Filter 활성화는 `EntityManager`를 통해 수행한다
- `TenantContext.get()`으로 tenant를 가져와 Filter에 주입한다
- tenant가 없으면 `TenantMissingException`을 throw한다

### 동작 흐름

```
요청 진입 (HTTP / Solace / 배치)
        ↓
TenantResolver.setTenant("tenantA")   ← 각 시스템 진입점에서 세팅
        ↓
AbstractCrudService 메서드 호출
        ↓
TenantContext.get() → Hibernate Filter 활성화
        ↓
모든 JPA 쿼리에 WHERE TENANT = 'tenantA' 자동 추가
        ↓
요청 완료 후 TenantResolver.clear()   ← 반드시 정리
```

### 완료 기준 체크리스트

- [ ] `TenantContext` ThreadLocal 정상 동작 확인
- [ ] `TenantMissingException` tenant 없을 때 throw 확인
- [ ] Hibernate Filter가 모든 조회 쿼리에 자동 적용 확인
- [ ] `clear()` 호출 후 다음 요청에 이전 tenant가 묻어나지 않는지 확인
- [ ] 단위 테스트 작성

---

## ✅ 아이템 2. 에러 처리 공통화

### 목적

JPA에서 발생하는 다양한 예외를 서비스 계층에서 일관된 방식으로 처리하고,
디버깅에 필요한 파라미터/쿼리 정보를 함께 로깅한다.

### 생성할 파일 목록

```
data/orm/common/error/
├── DataErrorCode.java          ← 에러 코드 enum
├── DataErrorResponse.java      ← 공통 에러 응답 DTO
└── JpaExceptionHandler.java    ← 예외 변환 공통 처리
```

### 상세 명세

#### DataErrorCode.java (enum)

```
NOT_FOUND         → EntityNotFoundException
UK_DUPLICATE      → DataIntegrityViolationException
INVALID_REQUEST   → ConstraintViolationException
LOCK_CONFLICT     → OptimisticLockingFailureException
DB_UNAVAILABLE    → JpaSystemException
TENANT_MISSING    → TenantMissingException
UNKNOWN           → 그 외 모든 예외
```

#### DataErrorResponse.java

- 필드 목록:
  - `DataErrorCode errorCode` — 에러 코드 enum
  - `String message` — 사람이 읽을 수 있는 설명
  - `String entity` — 어떤 엔티티에서 발생했는지 (클래스명)
  - `Object params` — 실행 시 넣은 파라미터
  - `String query` — 시도한 쿼리 정보 (메서드명 + 조건)
  - `LocalDateTime occurredAt` — 발생 시각
- Lombok `@Builder`, `@Getter` 적용
- 정적 팩토리 메서드 `of(DataErrorCode, String, String, Object, String)` 제공

#### JpaExceptionHandler.java

- `@Component`로 등록한다
- 메서드: `DataErrorResponse handle(Exception e, String entityName, Object params, String query)`
- 내부에서 exception 타입별로 분기하여 `DataErrorCode`를 결정한다
- 모든 예외는 동일 수준으로 `log.error()`로 로깅한다
- 로그 포맷: `[JPA ERROR] entityName={} | errorCode={} | params={} | query={} | message={}`

#### Optimistic Lock 재시도 정책

- `JpaExceptionHandler` 내부에 재시도 로직을 포함한다
- 재시도 조건: `OptimisticLockingFailureException` 발생 시
- 최대 재시도 횟수: 2회
- 총 허용 시간: 100ms 초과 시 즉시 `LOCK_CONFLICT` 반환
- 재시도 간격: 총 시간이 100ms를 넘지 않는 선에서 균등 분배

#### AbstractCrudService.java 수정

- 모든 메서드를 `JpaExceptionHandler`로 감싼다
- try-catch로 직접 감싸지 않고 `JpaExceptionHandler.handle()`에 위임한다
- `entityName`은 제네릭 타입에서 런타임에 추출한다

### 완료 기준 체크리스트

- [ ] 각 JPA 예외 → DataErrorCode 매핑 확인
- [ ] `params`, `query` 로그에 정상 출력 확인
- [ ] Optimistic Lock 재시도 2회 후 실패 확인
- [ ] 100ms 초과 시 즉시 실패 확인
- [ ] 단위 테스트 작성

---

## ✅ 아이템 1. UK별 CRUD 자동 생성

### 목적

엔티티의 `@UniqueConstraint` 선언을 런타임에 읽어,
UK 기반 조회 / 수정 / 삭제 메서드를 자동으로 처리한다.

### 생성할 파일 목록

```
data/orm/common/uk/
├── UkCrudService.java           ← UK CRUD 인터페이스
└── UkCrudSupport.java           ← UK 런타임 처리 공통 로직
```

### 상세 명세

#### UK 선언 방식

기존 JPA 표준 어노테이션 `@UniqueConstraint`를 재활용한다.
별도 어노테이션 없이 `name` 속성으로 UK를 구분한다.

```java
@Table(
    name = "GN_MSG_SRV_CONN",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_msg_srv", columnNames = {"ENV", "HOST", "PORT"})
    }
)
public class GnMsgSrvConnModel extends BaseModel { ... }
```

#### UkCrudService.java (인터페이스)

- 메서드:
  - `M findByUk(String ukName, Map<String, Object> params)`
  - `M updateByUk(String ukName, Map<String, Object> params, M model)`
  - `void deleteByUk(String ukName, Map<String, Object> params)`
- `ukName`은 `@UniqueConstraint(name = ...)` 의 name 속성값과 일치해야 한다

#### UkCrudSupport.java

- `AbstractCrudService`에서 사용하는 UK 처리 공통 로직
- **UK 정보 추출**: Java Reflection으로 엔티티 클래스의 `@Table` → `@UniqueConstraint` 를 읽는다
- **컬럼명 → 필드명 변환**: `@Column(name=...)` 어노테이션을 Reflection으로 읽어 매핑한다
- **동적 쿼리 생성**: `EntityManager`의 `CriteriaBuilder`를 사용한다 (JPQL 문자열 조합 금지)
- **UK 존재 검증**: 요청한 `ukName`이 엔티티에 없으면 `IllegalArgumentException`을 throw한다

#### findByUk 동작

1. `UkCrudSupport`로 해당 `ukName`의 컬럼 목록 추출
2. `params`의 키가 컬럼 목록과 일치하는지 검증
3. `CriteriaBuilder`로 동적 쿼리 생성 후 실행
4. 결과 없으면 `EntityNotFoundException` throw (DataErrorCode: `NOT_FOUND`)

#### updateByUk 동작

1. `findByUk`로 기존 엔티티 조회
2. `model`의 필드를 기존 엔티티에 복사 (null 필드는 무시)
3. UK 필드 자체는 업데이트에서 제외한다
4. `BasicAudit`의 `modifiedAt`, `modifiedBy`는 `@LastModifiedDate`, `@LastModifiedBy`로 자동 처리
5. `repository.save()`로 저장

#### deleteByUk 동작

1. `findByUk`로 기존 엔티티 조회
2. `useStatCd = UseStatCd.Delete` 로 Soft Delete 처리
3. `repository.save()`로 저장 (실제 DB 삭제 없음)

#### null 필드 무시 로직

```
model의 모든 필드를 Reflection으로 순회
    → 필드값이 null이면 skip
    → null이 아니면 기존 엔티티에 복사
```

#### AbstractCrudService.java 수정

- `UkCrudService` 인터페이스를 implement 한다
- `UkCrudSupport`를 주입받아 사용한다
- UK 관련 예외도 `JpaExceptionHandler`를 통해 처리한다

### DDL 전략

```yaml
# local / dev 환경 (befw-config-repo)
spring:
  jpa:
    hibernate:
      ddl-auto: update

# prod 환경 (추후 Flyway 도입 후 변경)
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

### 완료 기준 체크리스트

- [ ] `@UniqueConstraint`가 없는 엔티티에서 `findByUk` 호출 시 예외 확인
- [ ] 존재하지 않는 `ukName` 요청 시 `IllegalArgumentException` 확인
- [ ] `findByUk` 정상 조회 확인
- [ ] `updateByUk` null 필드 무시 + Audit 자동 업데이트 확인
- [ ] `deleteByUk` Soft Delete 확인 (실제 삭제 없음)
- [ ] 복합 UK (컬럼 2개 이상) 정상 동작 확인
- [ ] 단위 테스트 작성

---

## 🚫 공통 금지 사항

- JPQL 문자열 직접 조합 금지 → `CriteriaBuilder` 사용
- 실제 DB DELETE 금지 → 반드시 Soft Delete (`useStatCd = Delete`)
- tenant 없이 DB 접근 금지 → `TenantContext.get()` 필수
- `@Transactional` 누락 금지 → create / update / delete에 반드시 적용
- 기존 `AbstractCrudService`, `BaseModel`, `BasicAudit` 구조 변경 금지 (확장만 허용)

---

## 📎 참고 문서

- 설계 문서: `JPA 테이블 별 공통 함수 및 인터페이스 개발 방향 문서.md`
- 기존 엔티티 예시: `GnMsgSrvConnModel.java`
- 기존 서비스 예시: `GnMsgSrvConnAccess.java`
