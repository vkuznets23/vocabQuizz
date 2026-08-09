package com.vocabquizzz.vocab_quizz.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import com.vocabquizzz.vocab_quizz.model.WordConstraints

@Entity
@Table(name = "words")
class Word(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User = User(),

    @Column(nullable = false, length = WordConstraints.MAX_WORD_LENGTH)
    var word: String = "",

    @Column(nullable = false, length = WordConstraints.MAX_DEFINITION_LENGTH)
    var definition: String = "",

    @Column(length = WordConstraints.MAX_EXAMPLE_LENGTH)
    var example: String? = null,

    @Column(name = "created_at", insertable = false, updatable = false)
    val createdAt: LocalDateTime? = null
)