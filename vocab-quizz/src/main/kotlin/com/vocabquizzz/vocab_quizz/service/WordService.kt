package com.vocabquizzz.vocab_quizz.service

import org.springframework.stereotype.Service
import com.vocabquizzz.vocab_quizz.entity.Word
import com.vocabquizzz.vocab_quizz.entity.User
import com.vocabquizzz.vocab_quizz.model.EditableField
import com.vocabquizzz.vocab_quizz.repository.WordRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest


@Service
class WordService(
    private val wordRepository: WordRepository
){

    private val wordsPerPage = 20

    fun searchWords(
        user: User,
        query: String
    ): List<Word> =
        wordRepository.findTop5ByUserAndWordContainingIgnoreCase(user, query)

    fun getWords(user: User, page: Int): Page<Word> {
        return wordRepository.findAllByUser(
            user,
            PageRequest.of(page, wordsPerPage)
        )
    }
    
    fun getAllWords(user: User): List<Word> =
        wordRepository.findAllByUser(user)

    fun randomWord(user: User): Word =
        wordRepository.findAllByUser(user).random()

    fun addWord(
        user: User,
        word: String,
        definition: String,
        example: String?
    ): Word {

        val normalizedWord = word.trim()
        val normalizedDefinition = definition.trim()
        val normalizedExample = example?.trim()

        require(normalizedWord.isNotBlank()) {
            "Word cannot be empty."
        }

        require(normalizedDefinition.isNotBlank()) {
            "Definition cannot be empty."
        }

        require(word.length <= 100) {
            "Word cannot be longer than 100 characters."
        }

        require(definition.length <= 1000) {
            "Definition cannot be longer than 1000 characters."
        }

        if (wordRepository.existsByUserAndWord(user, word)) {
            throw IllegalArgumentException("Word already exists")
        }

        val newWord = Word(
            user = user,
            word = word,
            definition = definition,
            example = example
        )

        return wordRepository.save(newWord)
    }

    @Transactional
    fun deleteWord(
        user: User,
        word: String
    ): Boolean {
        return wordRepository.deleteByUserAndWord(user, word) > 0
    }

    fun getWord(
        user:User,
        word: String
    ): Word? {
        return wordRepository.findByUserAndWord(user, word)
    }

    fun countWords(user: User): Long =
        wordRepository.countByUser(user)

    @Transactional
    fun updateWord(
        id: Long,
        field: EditableField,
        value: String?
    ) {
        val word = wordRepository.findById(id).orElseThrow()

        when (field) {
            EditableField.WORD -> word.word = value!!
            EditableField.DEFINITION -> word.definition = value!!
            EditableField.EXAMPLE -> word.example = value
        }
    }
}