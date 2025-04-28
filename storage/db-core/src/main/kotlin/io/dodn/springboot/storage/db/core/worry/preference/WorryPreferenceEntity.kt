package io.dodn.springboot.storage.db.core.worry.preference

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * 고민 선호도 도메인 모델
 * 사용자의 고민 관련 선호 설정을 저장
 */
@Entity
@Table(name = "worry_preferences")
data class WorryPreferenceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    val userId: Long,

    @Enumerated(EnumType.STRING)
    val expressionStyle: DbExpressionStyle,

    @Enumerated(EnumType.STRING)
    val comfortStyle: DbComfortStyle,

    val createdAt: LocalDateTime,

    val updatedAt: LocalDateTime,
)
