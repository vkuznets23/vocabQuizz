package com.vocabquizzz.vocab_quizz.model

import com.vocabquizzz.vocab_quizz.model.Quiz

sealed interface StartQuizResult {
    data class Ready(val chatId: String, val quiz: Quiz) : StartQuizResult
    data object NotEnoughWords : StartQuizResult
    data object AlreadyRunning : StartQuizResult
}

sealed interface AnswerResult {
    data class NextQuestion(val chatId: String, val quiz: Quiz) : AnswerResult
    data class Finished(val chatId: String, val score: Int, val total: Int) : AnswerResult
    data object UnknownPoll : AnswerResult
}