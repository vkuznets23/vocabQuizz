package com.vocabquizzz.vocab_quizz.service.stats

import com.vocabquizzz.vocab_quizz.entity.User
import com.vocabquizzz.vocab_quizz.entity.Word
import com.vocabquizzz.vocab_quizz.repository.WordAttemptRepository
import com.vocabquizzz.vocab_quizz.service.word.WordService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class StatisticServiceTest {
    private val wordAttemptRepository: WordAttemptRepository = mock()
    private val wordService: WordService = mock()
    private val statisticService = StatisticService(wordAttemptRepository, wordService)

    private val user = User(id = 1, telegramUserId = 123)
    private val word = Word(id = 1, user = user, word = "apple", definition = "def")

    @Test
    fun `should return correct statistics`() {
        whenever(wordService.countWords(user)).thenReturn(25)
        whenever(wordAttemptRepository.countAttemptedWordsInDictionary(user)).thenReturn(10)
        whenever(wordAttemptRepository.countByUser(user)).thenReturn(54) // total attempts
        whenever(wordAttemptRepository.countByUserAndIsCorrect(user, true)).thenReturn(40)

        val stat = statisticService.getUserStatistic(user)

        assertEquals(25, stat.totalWords)
        assertEquals(10, stat.wordsAttempted)
        assertEquals(54, stat.totalAttempts)
        assertEquals(40, stat.correctAnswers)
        assertEquals(14, stat.wrongAnswers)
        assertEquals(40.0 / 54 * 100, stat.accuracy, 0.01)
    }

    @Test
    fun `no attempts should return zero statistics`() {
        whenever(wordService.countWords(user)).thenReturn(5)
        whenever(wordAttemptRepository.countAttemptedWordsInDictionary(user)).thenReturn(0)
        whenever(wordAttemptRepository.countByUser(user)).thenReturn(0)
        whenever(wordAttemptRepository.countByUserAndIsCorrect(user, true)).thenReturn(0)

        val stat = statisticService.getUserStatistic(user)
        assertEquals(5, stat.totalWords)
        assertEquals(0, stat.wordsAttempted)
        assertEquals(0, stat.totalAttempts)
        assertEquals(0, stat.correctAnswers)
        assertEquals(0, stat.wrongAnswers)
        assertEquals(0.0, stat.accuracy, 0.01)
    }

    @Test
    fun `getStatisticsForWord should return correct statistics`() {
        whenever(wordAttemptRepository.countByWord(word)).thenReturn(10)
        whenever(wordAttemptRepository.countByWordAndIsCorrect(word, true)).thenReturn(7)

        val stat = statisticService.getStatisticForWord(word)

        assertEquals("apple", stat.word)
        assertEquals(10, stat.totalAttempts)
        assertEquals(7, stat.correctAnswers)
        assertEquals(3, stat.wrongAnswers)
        assertEquals(70.0, stat.accuracy, 0.01)
    }

    @Test
    fun `no attempts should return zero statistics for word`() {
        whenever(wordAttemptRepository.countByWord(word)).thenReturn(0)
        whenever(wordAttemptRepository.countByWordAndIsCorrect(word, true)).thenReturn(0)

        val stat = statisticService.getStatisticForWord(word)
        assertEquals(0.0, stat.accuracy)
    }
}