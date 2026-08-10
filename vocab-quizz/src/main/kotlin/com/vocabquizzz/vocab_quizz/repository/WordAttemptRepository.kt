package com.vocabquizzz.vocab_quizz.repository

import com.vocabquizzz.vocab_quizz.entity.WordAttempt
import com.vocabquizzz.vocab_quizz.entity.Word
import com.vocabquizzz.vocab_quizz.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param


interface WordAttemptRepository : JpaRepository<WordAttempt, Long> {
    fun findAllByWord(word: Word): List<WordAttempt>
    fun countByWord(word: Word): Long
    fun countByUser(user: User): Long

    fun countByWordAndIsCorrect(
        word: Word,
        correct: Boolean
    ): Long 

    fun countByUserAndIsCorrect(
        user: User,
        correct: Boolean
    ): Long 

    @Query("""
        SELECT COUNT(DISTINCT w.id)
        FROM Word w
        WHERE w.user = :user
        AND EXISTS (
            SELECT 1 FROM WordAttempt wa
            WHERE wa.word = w AND wa.user = :user
        )
    """)
    fun countAttemptedWordsInDictionary(@Param("user") user: User): Long
}