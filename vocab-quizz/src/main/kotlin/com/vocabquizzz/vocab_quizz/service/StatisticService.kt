package com.vocabquizzz.vocab_quizz.service

import org.springframework.stereotype.Service
import com.vocabquizzz.vocab_quizz.model.UserStatistic
import com.vocabquizzz.vocab_quizz.model.WordStatistic
import com.vocabquizzz.vocab_quizz.entity.Word
import com.vocabquizzz.vocab_quizz.entity.User
import com.vocabquizzz.vocab_quizz.repository.WordAttemptRepository
import com.vocabquizzz.vocab_quizz.service.WordService


@Service
class StatisticService(
    private val wordAttemptRepository: WordAttemptRepository,
    private val wordService: WordService
){
    private fun calculateAccuracy(correct: Long, total: Long): Double =
        if (total == 0L) 0.0 else correct.toDouble() / total * 100

    fun getStatisticForWord(
        word: Word
    ): WordStatistic {
        val totalAttempts =
            wordAttemptRepository.countByWord(word)

        val correctAnswers =
            wordAttemptRepository.countByWordAndIsCorrect(
                word,
                true
            )

        val wrongAnswers =
            totalAttempts - correctAnswers

        val accuracy = calculateAccuracy(correctAnswers, totalAttempts)

        return WordStatistic(
            word = word.word,
            totalAttempts = totalAttempts,
            correctAnswers = correctAnswers,
            wrongAnswers = wrongAnswers,
            accuracy = accuracy
        )
    } 

    fun getUserStatistic(user: User): UserStatistic {
        val totalWords = wordService.countWords(user)
        val wordsAttempted =
            wordAttemptRepository.countDistinctWordsByUser(user)
        val totalAttempts =
            wordAttemptRepository.countByUser(user)

        val correctAnswers =
            wordAttemptRepository.countByUserAndIsCorrect(
                user,
                true
            )

        val wrongAnswers =
            totalAttempts - correctAnswers

        val accuracy = calculateAccuracy(correctAnswers, totalAttempts)

        return UserStatistic(
            totalWords = totalWords,
            wordsAttempted = wordsAttempted,
            totalAttempts = totalAttempts,
            correctAnswers = correctAnswers,
            wrongAnswers = wrongAnswers,
            accuracy = accuracy
        )
    }
}