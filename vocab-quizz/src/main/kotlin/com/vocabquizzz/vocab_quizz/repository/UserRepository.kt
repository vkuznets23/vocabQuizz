package com.vocabquizzz.vocab_quizz.repository

import com.vocabquizzz.vocab_quizz.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findByTelegramUserId(telegramUserId: Long): User?
}