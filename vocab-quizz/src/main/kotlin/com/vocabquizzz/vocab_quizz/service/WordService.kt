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

        require(normalizedWord.length <= 50) {
            "Word cannot be longer than 50 characters."
        }

        require(normalizedDefinition.length <= 200) {
            "Definition cannot be longer than 200 characters."
        }

        require(
            normalizedExample == null || normalizedExample.length <= 200
        ) {
            "Example cannot be longer than 200 characters."
        }

        if (wordRepository.existsByUserAndWordIgnoreCase(user, normalizedWord)) {
            throw IllegalArgumentException("Word already exists")
        }

        val newWord = Word(
            user = user,
            word = normalizedWord,
            definition = normalizedDefinition,
            example = normalizedExample
        )

        return wordRepository.save(newWord)
    }

    @Transactional
    fun deleteWord(
        user: User,
        word: String
    ): Boolean {
        return wordRepository.deleteByUserAndWordIgnoreCase(user, word) > 0
    }

    fun getWord(
        user:User,
        word: String
    ): Word? {
        return wordRepository.findByUserAndWordIgnoreCase(user, word.trim())
    }

    fun findByIdAndUser(
        wordId: Long,
        user: User
    ): Word? {
        return wordRepository.findByIdAndUser(wordId, user)
    }

    fun countWords(user: User): Long =
        wordRepository.countByUser(user)

    @Transactional
    fun updateWord(
        user: User,
        wordId: Long,
        field: EditableField,
        value: String?
    ) {
        val word = findByIdAndUser(wordId, user)
            ?: throw IllegalArgumentException("Word not found")

        when (field) {

            EditableField.WORD -> {
                val newWord = value?.trim()

                require(!newWord.isNullOrBlank()) {
                    "Word cannot be empty."
                }

                require(newWord.length <= 50) {
                    "Word cannot be longer than 50 characters."
                }

                if (
                    wordRepository.existsByUserAndWordIgnoreCase(user, newWord) &&
                    !word.word.equals(newWord, ignoreCase = true)
                ) {
                    throw IllegalArgumentException("Word already exists.")
                }

                word.word = newWord
            }

            EditableField.DEFINITION -> {
                val newDefinition = value?.trim()

                require(!newDefinition.isNullOrBlank()) {
                    "Definition cannot be empty."
                }

                require(newDefinition.length <= 200) {
                    "Definition cannot be longer than 200 characters."
                }

                word.definition = newDefinition
            }

            EditableField.EXAMPLE -> {
                val newExample = value?.trim()

                require(newExample == null || newExample.length <= 200) {
                    "Example cannot be longer than 200 characters."
                }

                word.example = newExample
            }
        }
    }
}