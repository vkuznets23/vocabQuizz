package com.vocabquizzz.vocab_quizz.model.stats

data class UserStatistic(
    val totalWords: Long,
    val wordsAttempted: Long,
    val totalAttempts: Long,
    val correctAnswers: Long,
    val wrongAnswers: Long,
    val accuracy: Double
)