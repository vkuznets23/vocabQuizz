package com.vocabquizzz.vocab_quizz.entity

import jakarta.persistence.*
import java.time.LocalTime
import java.time.Instant

@Entity
@Table(name = "user_quiz_settings")
class UserQuizSettings(

    @Id
    @Column(name = "user_id")
    val userId: Long = 0,

    // one user can have only one quiz settings
    // LAZY to avoid loading user data when need settings only
    @OneToOne (fetch = FetchType.LAZY) 
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    val user: User = User(),

    @Column(name = "enabled", nullable = false)
    var enabled: Boolean = false,

    @Column(name = "interval_hours", nullable = false)
    var intervalHours: Int = 2,

    @Column(name = "window_start", nullable = false)
    var windowStart: LocalTime = LocalTime.of(9, 0),

    @Column(name = "window_end", nullable = false)
    var windowEnd: LocalTime = LocalTime.of(18, 0),

    @Column(name = "timezone", nullable = false, length = 64)
    var timezone: String = "UTC",

    @Column(name = "questions_per_lesson", nullable = false)
    var questionsPerLesson: Int = 5,

    @Column(name = "last_quiz_sent_at")
    var lastQuizSentAt: Instant? = null,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
)