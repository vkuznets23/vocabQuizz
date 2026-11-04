package com.vocabquizzz.vocab_quizz.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import com.vocabquizzz.vocab_quizz.model.deck.DeckDefaults

@Entity
@Table(name = "decks")
class Deck(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User = User(),
    
    @Column(name = "name", nullable = false, length = 100)
    var name: String = DeckDefaults.GENERAL_NAME,

    @Column(name = "created_at", insertable = false, updatable = false)
    val createdAt: LocalDateTime? = null
) 
