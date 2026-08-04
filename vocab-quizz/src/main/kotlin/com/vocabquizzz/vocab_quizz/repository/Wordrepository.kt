package com.vocabquizzz.vocab_quizz.repository

import com.vocabquizzz.vocab_quizz.entity.Word
import com.vocabquizzz.vocab_quizz.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface WordRepository : JpaRepository<Word, Long> {
    fun findAllByUser(user: User): List<Word>
    fun existsByUserAndWord(user: User, word: String): Boolean
}