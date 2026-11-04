package com.vocabquizzz.vocab_quizz.service.user

import com.vocabquizzz.vocab_quizz.entity.User
import com.vocabquizzz.vocab_quizz.repository.UserRepository
import com.vocabquizzz.vocab_quizz.repository.UserQuizSettingsRepository
import com.vocabquizzz.vocab_quizz.entity.UserQuizSettings
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.vocabquizzz.vocab_quizz.service.deck.DeckService

@Service
class UserService(
    private val repository: UserRepository,
    private val settingsRepository: UserQuizSettingsRepository,
    private val deckService: DeckService,
) {
    @Transactional
    fun getOrCreate(telegramUserId: Long): User {
        val user = repository.findByTelegramUserId(telegramUserId)
            ?: run {
                val saved = repository.save(User(telegramUserId = telegramUserId))
                settingsRepository.save(
                    UserQuizSettings(user = repository.getReferenceById(saved.id))
                )
                saved
            }

        deckService.getOrCreateGeneral(user)
        return user
    }
}