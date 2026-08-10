package com.vocabquizzz.vocab_quizz.service.word

import org.springframework.stereotype.Service
import com.vocabquizzz.vocab_quizz.model.QuizType
import com.vocabquizzz.vocab_quizz.entity.User
import com.vocabquizzz.vocab_quizz.entity.WordAttempt
import com.vocabquizzz.vocab_quizz.repository.WordAttemptRepository
import com.vocabquizzz.vocab_quizz.service.word.WordService


@Service
class WordAttemptService(
    private val wordAttemptRepository: WordAttemptRepository,
    private val wordService: WordService,
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

   
}