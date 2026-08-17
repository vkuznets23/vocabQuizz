package com.vocabquizzz.vocab_quizz.service.user

import com.vocabquizzz.vocab_quizz.entity.User
import com.vocabquizzz.vocab_quizz.repository.UserRepository
import com.vocabquizzz.vocab_quizz.repository.UserQuizSettingsRepository
import com.vocabquizzz.vocab_quizz.entity.UserQuizSettings
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val repository: UserRepository,
    private val settingsRepository: UserQuizSettingsRepository,
) {

    @Transactional // because several operations are needed
    fun getOrCreate(telegramUserId: Long): User {

        repository.findByTelegramUserId(telegramUserId)?.let { return it }

        // create user and settings
    
        val user = repository.save(User(telegramUserId = telegramUserId)) // create a user with such tg id
        settingsRepository.save(
            UserQuizSettings(user = repository.getReferenceById(user.id))
        )
        return user
    }
}