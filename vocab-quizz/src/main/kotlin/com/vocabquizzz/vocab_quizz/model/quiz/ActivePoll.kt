package com.vocabquizzz.vocab_quizz.model.quiz

import com.vocabquizzz.vocab_quizz.model.quiz.QuizType

data class ActivePoll(
        val chatId: String,
        val wordId: Long,
        val correctOption: Int,
        val quizType: QuizType,
    )