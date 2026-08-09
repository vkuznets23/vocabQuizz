package com.vocabquizzz.vocab_quizz.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import com.vocabquizzz.vocab_quizz.model.QuizType

@Entity
@Table(name = "word_attempts")
class WordAttempt(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    val word: Word,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "is_correct", nullable = false)
    val isCorrect: Boolean,

    @Enumerated(EnumType.STRING)
    @Column(name = "quiz_type", nullable = false)
    val quizType: QuizType,

    @Column(name = "created_at", insertable = false, updatable = false)
    val createdAt: LocalDateTime? = null
)