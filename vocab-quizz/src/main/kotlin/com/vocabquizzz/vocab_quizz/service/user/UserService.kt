package com.vocabquizzz.vocab_quizz.service.user

import com.vocabquizzz.vocab_quizz.entity.User
import com.vocabquizzz.vocab_quizz.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val repository: UserRepository
) {

    fun getOrCreate(telegramUserId: Long): User {

        return repository.findByTelegramUserId(telegramUserId)
            ?: repository.save(
                User(
                    telegramUserId = telegramUserId
                )
            )
    }
}