package com.vocabquizzz.vocab_quizz.service

import org.springframework.stereotype.Service
import com.vocabquizzz.vocab_quizz.model.QuizType
import com.vocabquizzz.vocab_quizz.model.WordStatistic
import com.vocabquizzz.vocab_quizz.entity.User
import com.vocabquizzz.vocab_quizz.entity.WordAttempt
import com.vocabquizzz.vocab_quizz.repository.WordAttemptRepository
import com.vocabquizzz.vocab_quizz.service.WordService


@Service
class WordAttemptService(
    private val wordAttemptRepository: WordAttemptRepository,
    private val wordService: WordService

){

    fun recordAttempt(user: User, wordId: Long, quizType: QuizType, correct: Boolean): WordAttempt {
        val word = wordService.findByIdAndUser(wordId, user)
            ?: throw IllegalArgumentException("Word not found")

        val attempt = WordAttempt(
            word = word,
            user = user,
            quizType = quizType,
            isCorrect = correct
        ) 

        return wordAttemptRepository.save(attempt)
    }

    fun getStatisticForWord(
        user: User,
        wordText: String
    ): WordStatistic {
        val word = wordService.getWord(user, wordText)
            ?: throw IllegalArgumentException("Word not found")

        val totalAttempts =
            wordAttemptRepository.countByWord(word)

        val correctAnswers =
            wordAttemptRepository.countByWordAndIsCorrect(
                word,
                true
            )

        val wrongAnswers =
            totalAttempts - correctAnswers

        val accuracy =
            if (totalAttempts == 0L) {
                0.0
            } else {
                correctAnswers.toDouble() / totalAttempts * 100
            }

        return WordStatistic(
            word = word.word,
            totalAttempts = totalAttempts,
            correctAnswers = correctAnswers,
            wrongAnswers = wrongAnswers,
            accuracy = accuracy
        )
    } 

}