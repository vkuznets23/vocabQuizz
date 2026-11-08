package com.vocabquizzz.vocab_quizz.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import com.vocabquizzz.vocab_quizz.model.word.WordConstraints
import com.vocabquizzz.vocab_quizz.model.deck.DeckDefaults

@Entity
@Table(name = "words")
class Word(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User = User(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    val deck: Deck = Deck(user = User(), name = DeckDefaults.GENERAL_NAME),

    @Column(nullable = false, length = WordConstraints.MAX_WORD_LENGTH)
    var word: String = "",

    @Column(nullable = false, length = WordConstraints.MAX_DEFINITION_LENGTH)
    var definition: String = "",

    @Column(length = WordConstraints.MAX_EXAMPLE_LENGTH)
    var example: String? = null,

    @Column(name = "created_at", insertable = false, updatable = false)
    val createdAt: LocalDateTime? = null
) 