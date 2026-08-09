package com.vocabquizzz.vocab_quizz.repository

import com.vocabquizzz.vocab_quizz.entity.WordAttempt
import com.vocabquizzz.vocab_quizz.entity.Word
import org.springframework.data.jpa.repository.JpaRepository


interface WordAttemptRepository : JpaRepository<WordAttempt, Long> {
    fun findAllByWord(word: Word): List<WordAttempt>
    fun countByWord(word: Word): Long

    fun countByWordAndIsCorrect(
        word: Word,
        correct: Boolean
    ): Long 
}