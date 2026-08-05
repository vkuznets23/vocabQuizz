package com.vocabquizzz.vocab_quizz.service

import com.vocabquizzz.vocab_quizz.model.Quiz
import org.springframework.stereotype.Service
import kotlin.random.Random
import com.vocabquizzz.vocab_quizz.entity.Word
import com.vocabquizzz.vocab_quizz.entity.User
import com.vocabquizzz.vocab_quizz.repository.WordRepository


/**
 * Generates quiz questions from a user's vocabulary.
 *
 * Maintains a shuffled in-memory deck for each user so that words are
 * presented in random order without repetition until the deck is exhausted.
 * Each generated quiz is either a definition question or a sentence question.
 */
 
@Service
class QuizService(
    private val wordRepository: WordRepository,

) {
    private val decks = mutableMapOf<Long, MutableList<Word>>()

    fun getQuiz(user: User): Quiz {
        var deck = decks[user.id]

        if (deck == null || deck.isEmpty()) {
            deck = wordRepository.findAllByUser(user)
                .shuffled()
                .toMutableList()

            decks[user.id] = deck
        }

        val word = deck.removeFirst()

        return if (word.example == null || Random.nextInt(100) < 20)
            definitionQuiz(word)
        else
            sentenceQuiz(word)
    }

    private fun definitionQuiz(word: Word): Quiz {
        val wrong = wordRepository
            .findAllByUser(word.user)
            .filter { it.id != word.id }
            .shuffled()
            .take(3)
            .map { it.definition }

        val options = (wrong + word.definition).shuffled()

        return Quiz(
            question = "What does '${word.word}' mean?",
            options = options,
            correct = word.definition
        )
    }

    private fun sentenceQuiz(word: Word): Quiz {
        val wrong = wordRepository
            .findAllByUser(word.user)
            .filter { it.id != word.id }
            .shuffled()
            .take(3)
            .map { it.word }

        val options = (wrong + word.word).shuffled()

        return Quiz(
            question = word.example ?: "",
            options = options,
            correct = word.word
        )
    }
}