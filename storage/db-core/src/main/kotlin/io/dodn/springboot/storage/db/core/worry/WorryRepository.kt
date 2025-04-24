package io.dodn.springboot.storage.db.core.worry

import org.springframework.data.jpa.repository.JpaRepository

interface WorryRepository : JpaRepository<WorryEntity, Long>
