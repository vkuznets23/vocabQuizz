package com.vocabquizzz.vocab_quizz.service

import org.springframework.stereotype.Service
import com.vocabquizzz.vocab_quizz.entity.Word
import com.vocabquizzz.vocab_quizz.entity.User
import com.vocabquizzz.vocab_quizz.model.EditableField
import com.vocabquizzz.vocab_quizz.repository.WordRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import com.vocabquizzz.vocab_quizz.model.WordConstraints
import com.vocabquizzz.vocab_quizz.model.WordLookupResult


@Service
class WordService(
    private val wordRepository: WordRepository
){

    // ── Private: validation & normalization ───────────

    private fun validateWord(word: String) {
        require(word.isNotBlank()) {
            "Word cannot be empty."
        }

        require(word.length <= WordConstraints.MAX_WORD_LENGTH) {
            "Word cannot be longer than ${WordConstraints.MAX_WORD_LENGTH} characters."
        }
    }

    private fun validateDefinition(definition: String) {
        require(definition.isNotBlank()) {
            "Definition cannot be empty."
        }

        require(definition.length <= WordConstraints.MAX_DEFINITION_LENGTH) {
            "Definition cannot be longer than ${WordConstraints.MAX_DEFINITION_LENGTH} characters."
        }
    }
    
    private fun validateExample(example: String?) {
        require(example == null || example.isNotBlank()) {
            "Example cannot be empty."
        }

        require(example == null || example.length <= WordConstraints.MAX_EXAMPLE_LENGTH) {
            "Example cannot be longer than ${WordConstraints.MAX_EXAMPLE_LENGTH} characters."
        }
    }

    // ── Public: validation helpers ────────────────────

    fun validateWordInput(raw: String) {
        validateWord(raw.trim())
    }

    fun validateDefinitionInput(raw: String) {
        validateDefinition(raw.trim())
    }

    fun validateExampleInput(raw: String?) {
        validateExample(raw?.trim()?.takeIf { it.isNotBlank() })
    }

    fun ensureWordAvailable(user: User, word: String) {
        val normalized = word.trim()
        validateWord(normalized)
        if (wordRepository.existsByUserAndWordIgnoreCase(user, normalized)) {
            throw IllegalArgumentException("Word already exists")
        }
    }

    // ── Read ──────────────────────────────────────────

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

    fun searchWords(
        user: User,
        query: String
    ): List<Word> =
        wordRepository.findTop5ByUserAndWordContainingIgnoreCase(user, query)

    fun getWords(user: User, page: Int): Page<Word> {
        return wordRepository.findAllByUser(
            user,
            PageRequest.of(page, WordConstraints.MAX_WORDS_PER_PAGE)
        )
    }
    
    fun getAllWords(user: User): List<Word> =
        wordRepository.findAllByUser(user)

    fun countWords(user: User): Long =
        wordRepository.countByUser(user)
    
    fun hasEnoughWordsForQuiz(user: User): Boolean =
        countWords(user) >= WordConstraints.MIN_WORDS_FOR_QUIZ

    // ── Write ──────────────────────────────────────────

    @Transactional
    fun addWord(
        user: User,
        word: String,
        definition: String,
        example: String?
    ): Word {
        val normalizedWord = word.trim()
        val normalizedDefinition = definition.trim()
        val normalizedExample = example?.trim()?.takeIf { it.isNotBlank() }


        validateWord(normalizedWord)
        validateDefinition(normalizedDefinition)
        validateExample(normalizedExample)

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
        return wordRepository.deleteByUserAndWordIgnoreCase(user, word.trim()) > 0
    }

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
                val newWord = value?.trim() ?: throw IllegalArgumentException("Word cannot be empty.")
                validateWord(newWord)

                if (
                    wordRepository.existsByUserAndWordIgnoreCase(user, newWord) &&
                    !word.word.equals(newWord, ignoreCase = true)
                ) {
                    throw IllegalArgumentException("Word already exists")
                }

                word.word = newWord
            }

            EditableField.DEFINITION -> {
                val newDefinition = value?.trim() ?: throw IllegalArgumentException("Definition cannot be empty.")
                validateDefinition(newDefinition)
                word.definition = newDefinition
            }

            EditableField.EXAMPLE -> {
                val newExample = value?.trim()?.takeIf { it.isNotBlank() }
                validateExample(newExample)
                word.example = newExample
            }
        }
    }

    fun lookupWord(user: User, query: String): WordLookupResult {
        val normalized = query.trim()
        getWord(user, normalized)?.let { return WordLookupResult.Found(it) }

        val suggestions = searchWords(user, normalized)
        return if (suggestions.isEmpty()) WordLookupResult.NotFound
           else WordLookupResult.Suggestions(normalized, suggestions)
    }
}