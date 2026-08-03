package com.vocabquizzz.vocab_quizz.repository

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.vocabquizzz.vocab_quizz.model.Word
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Repository
import kotlin.random.Random

@Repository
class WordRepository(
    private val objectMapper: ObjectMapper
) {
    private val words = loadWords()

    private fun loadWords(): List<Word> {
        val resource = ClassPathResource("words.json")

        return objectMapper.readValue(
            resource.inputStream,
            object : TypeReference<List<Word>>() {}
        )
    }

    fun getAllWords(): List<Word> = words
}