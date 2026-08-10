package com.vocabquizzz.vocab_quizz.service

import com.vocabquizzz.vocab_quizz.entity.User
import com.vocabquizzz.vocab_quizz.entity.Word
import org.springframework.stereotype.Service

@Service
class QuizDeckService(
    private val wordService: WordService,
) {
    private val decks = mutableMapOf<Long, MutableList<Word>>()

    fun resetDeck(user: User) {
        decks[user.id!!] = wordService.getAllWords(user).shuffled().toMutableList()
    }

    fun nextWord(user: User): Word {
        val deck = decks.getOrPut(user.id!!) {
            wordService.getAllWords(user).shuffled().toMutableList()
        }
        if (deck.isEmpty()) {
            deck.addAll(wordService.getAllWords(user).shuffled())
        }
        return deck.removeFirst()
    }

    fun clearDeck(user: User) {
        decks.remove(user.id!!)
    }
}