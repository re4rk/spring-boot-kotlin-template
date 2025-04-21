# 고민냥 코드 컨벤션

## 레이어별 객체 네이밍 규칙

### Controller 레이어
- **입력**: `{Resource}Request` 또는 `{Action}{Resource}Request`
- **출력**: `{Resource}Response` 또는 `{Action}{Resource}Response`

### Facade 레이어 (선택적)
- **입력**: `{Resource}Command` 또는 `{Action}{Resource}Command`
- **출력**: `{Resource}Dto` 또는 `{Action}{Resource}Result`

### Domain 레이어
- **입력**: `{Resource}Params` 또는 `{Action}Params`
- **출력**: `{Resource}Info` 또는 도메인 객체

## 주요 원칙

1. **DTO는 Application 레이어에 배치**: 도메인 레이어는 비즈니스 로직에 집중하고 외부 의존성을 갖지 않아야 합니다.

2. **Parameter Object 사용 기준**: 파라미터가 3~4개 이상인 경우 Params 객체 사용을 고려하고, 5개 이상이면 적극 권장합니다.

3. **식별자(ID) 처리**: 주요 식별자(userId 등)는 Params 객체와 별도로 메서드 파라미터로 분리합니다.
   ```kotlin
   // 권장
   fun updateUserProfile(userId: Long, params: UserProfileUpdateParams): UserInfo
   
   // 지양
   fun updateUserProfile(params: UserProfileUpdateParams): UserInfo // userId가 params 내부에 있음
   ```

4. **객체 변환 책임**: 각 레이어 경계에서 적절한 객체 변환이 이루어져야 합니다.
    - Controller → Facade: Request → Command
    - Facade → Domain: Command → Params

## 예시 코드

```kotlin
// Controller 레이어
class UserRegistrationRequest(val email: String, val password: String, val name: String)

// Facade 레이어
class RegisterUserCommand(val email: String, val password: String, val name: String)

// Service 레이어
class UserCreationParams(val email: String, val password: String, val name: String, val initialActive: Boolean = false)

// 서비스 메서드 예시
@Transactional
fun register(params: UserCreationParams): UserInfo {
    val user = userStateProcessor.createUser(email = params.email, name = params.name)
    userPasswordManager.changePassword(user.id, params.password)
    
    if (params.initialActive) {
        userStateProcessor.activate(user.id)
    }
    
    return user
}
```
