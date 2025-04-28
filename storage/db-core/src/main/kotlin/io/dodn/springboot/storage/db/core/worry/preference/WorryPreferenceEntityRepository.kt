package io.dodn.springboot.storage.db.core.worry.preference

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WorryPreferenceEntityRepository : JpaRepository<WorryPreferenceEntity, Long> {

    /**
     * 사용자 ID로 고민 선호도 설정 조회
     * @param userId 사용자 ID
     * @return 고민 선호도 설정 (없을 경우 null)
     */
    fun findByUserId(userId: Long): WorryPreferenceEntity?
}
