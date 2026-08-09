package com.vocabquizzz.vocab_quizz.model

import com.vocabquizzz.vocab_quizz.model.QuizType

data class ActivePoll(
        val chatId: String,
        val wordId: Long,
        val correctOption: Int,
        val quizType: QuizType,
    )