package com.vocabquizzz.vocab_quizz.service.deck

import com.vocabquizzz.vocab_quizz.entity.Deck
import com.vocabquizzz.vocab_quizz.entity.User
import com.vocabquizzz.vocab_quizz.model.deck.DeckDefaults
import com.vocabquizzz.vocab_quizz.repository.DeckRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeckService(
    private val deckRepository: DeckRepository,
) {

    companion object {
        private const val MAX_NAME_LENGTH = 100
    }

    
    private fun requireNotGeneral(name: String) {
        require(!name.equals(DeckDefaults.GENERAL_NAME, ignoreCase = true)) {
            "Cannot delete the default deck."
        }
    }
    
    fun validateDeckName(rawName: String): String {
        val name = rawName.trim()
        require(name.isNotBlank()) { "Deck name cannot be empty." }
        require(name.length <= MAX_NAME_LENGTH) {
        "Deck name cannot be longer than $MAX_NAME_LENGTH characters."
        }
        return name
    }
    
    @Transactional
    fun getOrCreateGeneral(user: User): Deck {
        deckRepository.findByUserAndNameIgnoreCase(user, DeckDefaults.GENERAL_NAME)?.let { return it }
        return deckRepository.save(Deck(user = user, name = DeckDefaults.GENERAL_NAME))
    }

    fun getAllDecks(user: User): List<Deck> =
        deckRepository.findAllByUserIdOrderByName(user.id)

    fun getByNameForUser(user: User, rawName: String): Deck? {
        val name = rawName.trim()
        if (name.isBlank()) return null
        return deckRepository.findByUserAndNameIgnoreCase(user, name)
    }

    fun findByIdForUser(deckId: Long, user: User): Deck? =
        deckRepository.findById(deckId).orElse(null)?.takeIf { it.user.id == user.id }

    @Transactional
    fun createDeck(user: User, rawName: String): Deck {
        val name = validateDeckName(rawName)
        if (deckRepository.findByUserAndNameIgnoreCase(user, name) != null) {
        throw IllegalArgumentException("Deck already exists")
        }
        return deckRepository.save(Deck(user = user, name = name))
    }

    @Transactional
    fun deleteDeck(user: User, rawName: String) {
        val name = rawName.trim()
        require(name.isNotBlank()) { "Deck name cannot be empty." }
        requireNotGeneral(name)

        val deck = deckRepository.findByUserAndNameIgnoreCase(user, name)
        ?: throw IllegalArgumentException("Deck not found")

        deckRepository.delete(deck)
    }

    @Transactional
    fun renameDeck(user: User, rawOldName: String, rawNewName: String): Deck {
        val oldName = rawOldName.trim()
        val newName = validateDeckName(rawNewName)

        val deck = deckRepository.findByUserAndNameIgnoreCase(user, oldName)
        ?: throw IllegalArgumentException("Deck not found")

        require(!deck.name.equals(DeckDefaults.GENERAL_NAME, ignoreCase = true)) {
            "Cannot rename the default deck."
        }

        if (deckRepository.findByUserAndNameIgnoreCase(user, newName) != null) {
            throw IllegalArgumentException("Deck already exists")
        }

        deck.name = newName
        return deckRepository.save(deck)
          
    }

}