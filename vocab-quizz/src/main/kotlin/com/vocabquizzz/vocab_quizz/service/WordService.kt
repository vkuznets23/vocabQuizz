package com.vocabquizzz.vocab_quizz.service

import org.springframework.stereotype.Service
import com.vocabquizzz.vocab_quizz.model.Word
import com.vocabquizzz.vocab_quizz.repository.WordRepository


@Service
class WordService(
    private val repository: WordRepository
) {

    fun getAllWords() = repository.getAllWords()

    fun randomWord(): Word =
        repository.getAllWords().random()
}