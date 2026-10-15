package com.vocabquizzz.vocab_quizz.model.quiz

data class QuizSession(
    val total: Int,
    var remaining: Int,
    var score: Int
)