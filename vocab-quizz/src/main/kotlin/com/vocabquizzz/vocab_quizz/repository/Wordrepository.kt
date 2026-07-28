package com.vocabquizzz.vocab_quizz.repository

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.vocabquizzz.vocab_quizz.model.Word
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Repository

@Repository
class WordRepository(
    private val objectMapper: ObjectMapper
) {

    private val words: List<Word> = loadWords()

    private fun loadWords(): List<Word> {
        val resource = ClassPathResource("words.json")

        return objectMapper.readValue(
            resource.inputStream,
            object : TypeReference<List<Word>>() {}
        )
    }

    fun getRandomWord(): Word {
        return words.random()
    }

    fun getRandomWrongOptions(correct: Word): List<String> {
        return words
            .filter { it.word != correct.word }
            .map { it.options }
            .flatten()
            .shuffled()
            .take(3)
    }
}