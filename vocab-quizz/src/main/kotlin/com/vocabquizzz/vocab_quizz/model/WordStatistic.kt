package com.vocabquizzz.vocab_quizz.model

data class WordStatistic(
    val word: String,
    val totalAttempts: Long,
    val correctAnswers: Long,
    val wrongAnswers: Long,
    val accuracy: Double
)