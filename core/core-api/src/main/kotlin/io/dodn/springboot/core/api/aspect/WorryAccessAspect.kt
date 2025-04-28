package io.dodn.springboot.core.api.aspect

import io.dodn.springboot.core.domain.worry.Worry
import io.dodn.springboot.core.domain.worry.WorryStorage
import io.dodn.springboot.core.support.auth.GominUserDetails
import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Aspect
@Component
class WorryAccessAspect(
    private val worryStorage: WorryStorage,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Around("@annotation(checkAccess)")
    fun checkAccess(joinPoint: ProceedingJoinPoint, checkAccess: CheckWorryAccess): Any {
        // 1. 메서드 파라미터에서 worryId 추출
        val worryId = extractWorryId(joinPoint, checkAccess.worryIdParam)

        // 2. 현재 인증된 사용자 정보 가져오기
        val authentication = SecurityContextHolder.getContext().authentication
        val userDetails = authentication.principal as GominUserDetails

        // 3. 걱정거리(Worry) 엔티티 조회
        val worry = worryStorage.getWorry(worryId)

        // 4. 권한 확인
        val hasPermission = when (checkAccess.permission) {
            "VIEW" -> canView(worry, userDetails)
            "EDIT" -> canEdit(worry, userDetails)
            "DELETE" -> canDelete(worry, userDetails)
            else -> false
        }

        // 5. 권한이 없으면 예외 발생
        if (!hasPermission) {
            logger.warn(ErrorType.WORRY_PERMISSION_DENIED.message)
            throw CoreException(ErrorType.WORRY_PERMISSION_DENIED)
        }

        // 6. 권한이 있으면 원래 메서드 실행
        return joinPoint.proceed()
    }

    private fun extractWorryId(joinPoint: ProceedingJoinPoint, paramName: String): Long {
        val signature = joinPoint.signature as MethodSignature
        val paramNames = signature.parameterNames
        val args = joinPoint.args

        val paramIndex = paramNames.indexOf(paramName)
        if (paramIndex == -1) {
            throw CoreException(ErrorType.INVALID_PARAMETER, "Parameter $paramName not found in method signature")
        }

        return args[paramIndex] as Long
    }

    private fun canView(worry: Worry, userDetails: GominUserDetails): Boolean {
        return worry.userId == userDetails.id
    }

    private fun canEdit(worry: Worry, userDetails: GominUserDetails): Boolean {
        return worry.userId == userDetails.id
    }

    private fun canDelete(worry: Worry, userDetails: GominUserDetails): Boolean {
        return worry.userId == userDetails.id
    }
}
