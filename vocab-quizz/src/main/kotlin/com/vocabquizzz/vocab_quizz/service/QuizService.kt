package com.vocabquizzz.vocab_quizz.service

import com.vocabquizzz.vocab_quizz.model.Quiz
import org.springframework.stereotype.Service
import kotlin.random.Random
import com.vocabquizzz.vocab_quizz.entity.Word
import com.vocabquizzz.vocab_quizz.entity.User
import com.vocabquizzz.vocab_quizz.repository.WordRepository


/**
 * Generates different quiz types from a user's vocabulary.
 *
 * Maintains a shuffled in-memory deck for each user so that words are
 * presented in random order without repetition until the deck is exhausted.
 */
 
@Service
class QuizService(
    private val wordRepository: WordRepository,

) {
    private val decks = mutableMapOf<Long, MutableList<Word>>()

    private fun otherWords(word: Word): List<Word> =
        wordRepository.findAllByUser(word.user)
            .filter { it.id != word.id }

    fun getQuiz(user: User): Quiz {
        var deck = decks[user.id]

        if (deck == null || deck.isEmpty()) {
            deck = wordRepository.findAllByUser(user)
                .shuffled()
                .toMutableList()

            decks[user.id] = deck
        }

        // do i need words < 4 error here?

        val word = deck.removeFirst()

        val quizzes = mutableListOf<(Word) -> Quiz>(
            ::wordToDefinitionQuiz,
            ::definitionToWordQuiz,
            ::trueFalseQuiz
        )

        if (word.example != null) {
            quizzes += ::sentenceQuiz
        }

        return quizzes.random()(word)
    }

    private fun definitionToWordQuiz(word: Word): Quiz {
        val wrong = otherWords(word)
            .shuffled()
            .take(3)
            .map { it.word }

        val options = (wrong + word.word).shuffled()

        return Quiz(
            question = word.definition,
            options = options,
            correct = word.word
        )

    }

    private fun wordToDefinitionQuiz(word: Word): Quiz {
        val wrong = otherWords(word)
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

    private fun trueFalseQuiz(word: Word): Quiz {
        val isTrue = Random.nextBoolean()

        val definition = 
            if (isTrue) {
                word.definition
            } else {
                otherWords(word).random().definition
            }
        
        return Quiz(
            question = """
                True or False?

                ${word.word}
                $definition
            """.trimIndent(),
            options = listOf("True", "False"),
            correct = if (isTrue) "True" else "False"
        )
    }

    private fun sentenceQuiz(word: Word): Quiz {
        val wrong = otherWords(word)
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