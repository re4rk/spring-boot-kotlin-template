package io.dodn.springboot.storage.db.core.counseling.session

import io.dodn.springboot.storage.db.core.BaseEntity
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.Table

@Entity
@Table(name = "counseling_sessions")
class CounselingSessionEntity(
    @Column(nullable = true)
    val userId: Long? = null,

    @Lob
    @Column(nullable = false)
    val userInput: String,

    @Lob
    @Column(nullable = false)
    val aiResponse: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var visibility: CounselingVisibility = CounselingVisibility.PRIVATE,

    @Column(nullable = true)
    var summary: String? = null,

    @Column(nullable = true)
    var title: String? = null,

    @ElementCollection
    @CollectionTable(
        name = "counseling_emotion_tags",
        joinColumns = [JoinColumn(name = "session_id")],
    )
    @Column(name = "tag")
    var emotionTags: List<String> = emptyList(),
) : BaseEntity()
