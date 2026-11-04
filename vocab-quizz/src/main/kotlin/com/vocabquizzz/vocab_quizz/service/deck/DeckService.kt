package com.vocabquizzz.vocab_quizz.service.deck

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.vocabquizzz.vocab_quizz.entity.User
import com.vocabquizzz.vocab_quizz.entity.Deck
import com.vocabquizzz.vocab_quizz.repository.DeckRepository
import com.vocabquizzz.vocab_quizz.model.deck.DeckDefaults

@Service
class DeckService(
    private val deckRepository: DeckRepository
) {

    @Transactional
    fun getOrCreateGeneral(user: User): Deck {
        val existing = deckRepository.findByUserAndNameIgnoreCase(user, DeckDefaults.GENERAL_NAME)
        if (existing != null) {
            return existing
        }

        val newDeck = Deck(user = user, name = DeckDefaults.GENERAL_NAME)
        return deckRepository.save(newDeck)
    }
}