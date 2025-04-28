package io.dodn.springboot.storage.db.core.worry.preference

/**
 * 표현 스타일 열거형
 * 사용자가 감정을 표현하는 방식에 대한 선호도
 */
enum class DbExpressionStyle {
    DIRECT, // 직설적 표현 스타일
    INDIRECT, // 간접적 표현 스타일
}

/**
 * 위로 스타일 열거형
 * 사용자가 선호하는 위로/피드백 방식
 */
enum class DbComfortStyle {
    PRACTICAL, // 실용적/해결책 중심 위로
    EMOTIONAL, // 정서적 공감 중심 위로
    SUPPORTIVE, // 지지/격려 중심 위로
}
