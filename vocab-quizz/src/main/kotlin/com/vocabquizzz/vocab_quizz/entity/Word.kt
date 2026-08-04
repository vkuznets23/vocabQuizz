package com.vocabquizzz.vocab_quizz.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "words")
class Word(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User = User(),

    @Column(nullable = false)
    val word: String = "",

    @Column(nullable = false)
    val definition: String = "",

    val example: String? = null,

    @Column(name = "created_at", insertable = false, updatable = false)
    val createdAt: LocalDateTime? = null
)