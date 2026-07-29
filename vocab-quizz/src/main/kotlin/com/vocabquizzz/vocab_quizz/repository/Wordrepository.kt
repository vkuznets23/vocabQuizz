package com.vocabquizzz.vocab_quizz.repository

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.vocabquizzz.vocab_quizz.model.Word
import com.vocabquizzz.vocab_quizz.model.Quiz
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Repository

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

    fun getQuiz(): Quiz {
        if (deck.isEmpty()) {
            deck = words.shuffled().toMutableList()
        }
        val word = deck.removeFirst()

        val wrongAnswers = words
            .filter {it.word != word.word}
            .shuffled()
            .take(3)
            .map {it.correct}

        val options = (wrongAnswers + word.correct).shuffled()

        return Quiz(word = word.word, correct = word.correct, options = options)
    }
}