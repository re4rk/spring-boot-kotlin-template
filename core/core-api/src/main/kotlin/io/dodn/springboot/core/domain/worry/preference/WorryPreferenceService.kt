package io.dodn.springboot.core.domain.worry.preference

import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.worry.preference.DbComfortStyle
import io.dodn.springboot.storage.db.core.worry.preference.DbExpressionStyle
import io.dodn.springboot.storage.db.core.worry.preference.WorryPreferenceEntity
import io.dodn.springboot.storage.db.core.worry.preference.WorryPreferenceEntityRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class WorryPreferenceService(
    private val worryPreferenceEntityRepository: WorryPreferenceEntityRepository,
) {

    /**
     * 사용자의 고민 선호도 설정 생성
     * @param userId 사용자 ID
     * @param params 고민 선호도 매개변수
     * @return 생성된 고민 선호도 설정
     */
    @Transactional
    fun createWorryPreference(userId: Long, params: WorryPreferenceParams): WorryPreference {
        // 이미 존재하는지 확인
        worryPreferenceEntityRepository.findByUserId(userId)?.let {
            throw CoreException(ErrorType.DUPLICATE_RESOURCE, "User already has worry preference settings")
        }

        val now = LocalDateTime.now()
        val preference = WorryPreferenceEntity(
            id = 0L,
            userId = userId,
            expressionStyle = DbExpressionStyle.valueOf(params.expressionStyle.name),
            comfortStyle = DbComfortStyle.valueOf(params.comfortStyle.name),
            createdAt = now,
            updatedAt = now,
        )

        return worryPreferenceEntityRepository.save(preference).toWorryPreference()
    }

    /**
     * 사용자 ID로 고민 선호도 설정 조회
     * @param userId 사용자 ID
     * @return 고민 선호도 설정 (없을 경우 기본값 생성 후 반환)
     */
    @Transactional(readOnly = true)
    fun getWorryPreferenceByUserId(userId: Long): WorryPreference {
        val worryPreferenceEntity =
            worryPreferenceEntityRepository.findByUserId(userId) ?: createDefaultWorryPreference(userId)
        return worryPreferenceEntity.toWorryPreference()
    }

    /**
     * 사용자의 고민 선호도 설정 업데이트
     * @param userId 사용자 ID
     * @param params 업데이트할 고민 선호도 매개변수
     * @return 업데이트된 고민 선호도 설정
     */
    @Transactional
    fun updateWorryPreference(userId: Long, params: WorryPreferenceParams): WorryPreference {
        val preference = worryPreferenceEntityRepository.findByUserId(userId)?.toWorryPreference()
            ?: return createWorryPreference(userId, params)

        // 업데이트 수행
        val updatedPreference = preference.copy(
            expressionStyle = params.expressionStyle,
            comfortStyle = params.comfortStyle,
        )

        return worryPreferenceEntityRepository.save(updatedPreference.toEntity()).toWorryPreference()
    }

    /**
     * 기본 고민 선호도 설정 생성
     * @param userId 사용자 ID
     * @return 생성된 기본 고민 선호도 설정
     */
    @Transactional
    fun createDefaultWorryPreference(userId: Long): WorryPreferenceEntity {
        val now = LocalDateTime.now()
        val defaultPreference = WorryPreferenceEntity(
            id = 0L,
            userId = userId,
            expressionStyle = DbExpressionStyle.DIRECT,
            comfortStyle = DbComfortStyle.SUPPORTIVE,
            createdAt = now,
            updatedAt = now,
        )

        return worryPreferenceEntityRepository.save(defaultPreference)
    }

    private fun WorryPreferenceEntity.toWorryPreference(): WorryPreference {
        return WorryPreference(
            id = this.id,
            userId = this.userId,
            expressionStyle = ExpressionStyle.valueOf(this.expressionStyle.name),
            comfortStyle = ComfortStyle.valueOf(this.comfortStyle.name),
        )
    }

    private fun WorryPreference.toEntity(): WorryPreferenceEntity {
        return WorryPreferenceEntity(
            id = this.id,
            userId = this.userId,
            expressionStyle = DbExpressionStyle.valueOf(this.expressionStyle.name),
            comfortStyle = DbComfortStyle.valueOf(this.comfortStyle.name),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
    }
}
