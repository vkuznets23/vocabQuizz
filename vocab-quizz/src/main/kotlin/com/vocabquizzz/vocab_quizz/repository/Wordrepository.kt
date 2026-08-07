package com.vocabquizzz.vocab_quizz.repository

import com.vocabquizzz.vocab_quizz.entity.Word
import com.vocabquizzz.vocab_quizz.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable


interface WordRepository : JpaRepository<Word, Long> {
    fun findAllByUser(user: User): List<Word>
    fun findAllByUser(
        user: User,
        pageable: Pageable
    ): Page<Word>
    fun findByUserAndWord(user: User, word: String): Word?
    fun existsByUserAndWord(user: User, word: String): Boolean
    fun deleteByUserAndWord(user: User, word: String): Long
    fun countByUser(user: User): Long
}