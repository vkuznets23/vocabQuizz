package com.vocabquizzz.vocab_quizz.repository

import com.vocabquizzz.vocab_quizz.entity.Deck
import com.vocabquizzz.vocab_quizz.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface DeckRepository : JpaRepository<Deck, Long> {
    fun findByUserId(userId: Long): List<Deck>
    fun findByUserAndNameIgnoreCase(user:User, name: String): Deck?
    fun findAllByUserIdOrderByName(userId: Long): List<Deck>
}