package com.vocabquizzz.vocab_quizz.service.word

import com.vocabquizzz.vocab_quizz.entity.User
import com.vocabquizzz.vocab_quizz.entity.Word
import com.vocabquizzz.vocab_quizz.model.DeleteWordResult
import com.vocabquizzz.vocab_quizz.model.WordConstraints
import com.vocabquizzz.vocab_quizz.model.WordLookupResult
import com.vocabquizzz.vocab_quizz.repository.WordRepository
import com.vocabquizzz.vocab_quizz.service.word.WordService
import com.vocabquizzz.vocab_quizz.model.EditableField
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WordServiceTest {
    private val wordRepository: WordRepository = mock()
    private val wordService = WordService(wordRepository)
    private val user = User(id = 1, telegramUserId = 123)

    @Test
    fun `hasEnoughWordsForQuiz returns false when below minimum`() {
        whenever(wordRepository.countByUser(user)).thenReturn(3)
        assertFalse(wordService.hasEnoughWordsForQuiz(user))
    }

    @Test
    fun `hasEnoughWordsForQuiz returns true when equal to minimum`() {
        whenever(wordRepository.countByUser(user)).thenReturn(WordConstraints.MIN_WORDS_FOR_QUIZ.toLong())
        assertTrue(wordService.hasEnoughWordsForQuiz(user))
    }

    @Test
    fun `hasEnoughWordsForQuiz returns true when above minimum`() {
        whenever(wordRepository.countByUser(user)).thenReturn(WordConstraints.MIN_WORDS_FOR_QUIZ.toLong() + 1)
        assertTrue(wordService.hasEnoughWordsForQuiz(user))
    }

    @Test
    fun `addWord saves normalized word`() {
        whenever(wordRepository.existsByUserAndWordIgnoreCase(user, "apple"))
            .thenReturn(false)
        whenever(wordRepository.save(org.mockito.kotlin.any()))
            .thenAnswer { it.arguments[0] as Word }
        
        val saved = wordService.addWord(user, "  apple  ", "  a fruit  ", "  ")
        assertEquals("apple", saved.word)
        assertEquals("a fruit", saved.definition)
        assertEquals(null, saved.example)
    }

    @Test
    fun `addWord throws when word is too long`() {
        val error = assertThrows<IllegalArgumentException> {
            wordService.addWord(user, "a".repeat(WordConstraints.MAX_WORD_LENGTH + 1), "a fruit", null)
        }
        assertEquals("Word cannot be longer than ${WordConstraints.MAX_WORD_LENGTH} characters.", error.message)
    }

    @Test
    fun `addWord throws when definition is too long`() {
        val error = assertThrows<IllegalArgumentException> {
            wordService.addWord(user, "app", "a fruit".repeat(WordConstraints.MAX_DEFINITION_LENGTH + 1), null)
        }
        assertEquals("Definition cannot be longer than ${WordConstraints.MAX_DEFINITION_LENGTH} characters.", error.message)
    }

    @Test
    fun `addWord throws when example is too long`() {
        val error = assertThrows<IllegalArgumentException> {
            wordService.addWord(user, "app", "a fruit", "e".repeat(WordConstraints.MAX_EXAMPLE_LENGTH + 1))
        }
        assertEquals("Example cannot be longer than ${WordConstraints.MAX_EXAMPLE_LENGTH} characters.", error.message)
    }

    @Test
    fun `addWord throws when word is empty`() {
        val error = assertThrows<IllegalArgumentException> {
            wordService.addWord(user, "", "a fruit", null)
        }
        assertEquals("Word cannot be empty.", error.message)
    }

    @Test
    fun `addWord throws when definition is empty`() {
        val error = assertThrows<IllegalArgumentException> {
            wordService.addWord(user, "app", "", null)
        }
        assertEquals("Definition cannot be empty.", error.message)
    }

    @Test
    fun `addWord throws when word already exists`() {
        whenever(wordRepository.existsByUserAndWordIgnoreCase(user, "apple"))
            .thenReturn(true)
        
        val error = assertThrows<IllegalArgumentException> {
            wordService.addWord(user, "apple", "a fruit", null)
        }

        assertEquals("Word already exists", error.message)
    }

    @Test
    fun `deleteWord returns NotFound when word not found`() {
        whenever(wordRepository.deleteByUserAndWordIgnoreCase(user, "apple"))
            .thenReturn(0)

        val result = wordService.deleteWord(user, "apple")
        assertTrue(result is DeleteWordResult.NotFound)
    }

    @Test
    fun `deleteWord returns Deleted when word exists`() {
        whenever(wordRepository.deleteByUserAndWordIgnoreCase(user, "apple"))
            .thenReturn(1)
        val result = wordService.deleteWord(user, "apple")
        assertTrue(result is DeleteWordResult.Deleted)
    }

    @Test
    fun `updateWord updates definition with trim`() {
        val word = existingWord()
        whenever(wordRepository.findByIdAndUser(1, user)).thenReturn(word)

        wordService.updateWord(user, 1, EditableField.DEFINITION, "  new def  ")

        assertEquals("new def", word.definition)
    }

    @Test
    fun `updateWord throws when word not found`() {
        whenever(wordRepository.findByIdAndUser(1, user))
            .thenReturn(null)

        val error = assertThrows<IllegalArgumentException> {
            wordService.updateWord(user, 1, EditableField.WORD, "apple")
        }
        assertEquals("Word not found", error.message)
    }

    private fun existingWord() =
    Word(id = 1, user = user, word = "apple", definition = "a fruit", example = "I like apple")

    @Test
    fun `updateWord throws when word value is empty`() {
        whenever(wordRepository.findByIdAndUser(1, user))
            .thenReturn(existingWord())

        val error = assertThrows<IllegalArgumentException> {
            wordService.updateWord(user, 1, EditableField.WORD, "")
        }
        assertEquals("Word cannot be empty.", error.message)
    }

    @Test
    fun `updateWord throws when definition value is empty`() {
        whenever(wordRepository.findByIdAndUser(1, user))
            .thenReturn(existingWord())

        val error = assertThrows<IllegalArgumentException> {
            wordService.updateWord(user, 1, EditableField.DEFINITION, "   ")
        }
        assertEquals("Definition cannot be empty.", error.message)
    }

    @Test
    fun `updateWord clears example when value is blank`() {
        val word = existingWord()
        whenever(wordRepository.findByIdAndUser(1, user))
            .thenReturn(word)

        wordService.updateWord(user, 1, EditableField.EXAMPLE, "   ")

        assertEquals(null, word.example)
    }

    @Test
    fun `updateWord throws when renaming to existing word`() {
        val word = existingWord()
        whenever(wordRepository.findByIdAndUser(1, user)).thenReturn(word)
        whenever(wordRepository.existsByUserAndWordIgnoreCase(user, "banana"))
            .thenReturn(true)

        val error = assertThrows<IllegalArgumentException> {
            wordService.updateWord(user, 1, EditableField.WORD, "banana")
        }
        assertEquals("Word already exists", error.message)
    }

    @Test
    fun `ensureWordAvailable passes when word is free`() {
        whenever(wordRepository.existsByUserAndWordIgnoreCase(user, "apple"))
            .thenReturn(false)

        wordService.ensureWordAvailable(user, "apple") 
    }

    @Test
    fun `ensureWordAvailable throws when word already exists`() {
        whenever(wordRepository.existsByUserAndWordIgnoreCase(user, "apple"))
            .thenReturn(true)
        
        val error = assertThrows<IllegalArgumentException> {
            wordService.ensureWordAvailable(user, "apple")
        }
        assertEquals("Word already exists", error.message)
    }

    @Test
    fun `lookupWord returns Found for exact match`() {
        val word = Word(id = 1, user = user, word = "apple", definition = "a fruit")

        whenever(wordRepository.findByUserAndWordIgnoreCase(user, "apple"))
            .thenReturn(word)
    
        val result = wordService.lookupWord(user, "apple")
        assertTrue(result is WordLookupResult.Found)
        assertEquals(word, result.word)
    }

    @Test
    fun `lookupWord returns Suggestions when no exact match`() {
        val suggestions = listOf(
            Word(id = 1, user = user, word = "apple", definition = "a fruit"),
        )
        whenever(wordRepository.findByUserAndWordIgnoreCase(user, "app"))
            .thenReturn(null)
        whenever(wordRepository.findTop5ByUserAndWordContainingIgnoreCase(user, "app"))
            .thenReturn(suggestions)
        val result = wordService.lookupWord(user, "app")
        assertTrue(result is WordLookupResult.Suggestions)
        assertEquals("app", result.query)
        assertEquals(suggestions, result.words)
    }

    @Test
    fun `lookupWord returns NotFound when no exact match`() {
        whenever(wordRepository.findByUserAndWordIgnoreCase(user, "apple"))
            .thenReturn(null)
        whenever(wordRepository.findTop5ByUserAndWordContainingIgnoreCase(user, "apple"))
            .thenReturn(emptyList())
    
        val result = wordService.lookupWord(user, "apple")
        assertTrue(result is WordLookupResult.NotFound)
    }

    
}