package com.vocabquizzz.vocab_quizz.repository

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.vocabquizzz.vocab_quizz.model.Word
import com.vocabquizzz.vocab_quizz.model.Quiz
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Repository
import kotlin.random.Random

@Repository
class WordRepository(
    private val objectMapper: ObjectMapper
) {
    private val words: List<Word> = loadWords()
    private var deck: MutableList<Word> = words.shuffled().toMutableList()

    private fun loadWords(): List<Word> {
        val resource = ClassPathResource("words.json")

        return objectMapper.readValue(
            resource.inputStream,
            object : TypeReference<List<Word>>() {}
        )
    }

    private fun definitionQuiz(word: Word): Quiz {
        val wrong = words
            .filter { it.word != word.word }
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
        val wrong = words
            .filter { it.word != word.word }
            .shuffled()
            .take(3)
            .map { it.word }

        val options = (wrong + word.word).shuffled()
        return Quiz(question = word.example, options = options, correct = word.word)
    }

    fun getQuiz(): Quiz {
        if (deck.isEmpty()) {
            deck = words.shuffled().toMutableList()
        }
        val word = deck.removeFirst()

        return if (Random.nextInt(100) < 20) {
            definitionQuiz(word)
        }else {
            sentenceQuiz(word)
        }
    }
}