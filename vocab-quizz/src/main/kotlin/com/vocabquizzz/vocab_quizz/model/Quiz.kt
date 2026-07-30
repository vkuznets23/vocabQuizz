package com.vocabquizzz.vocab_quizz.model

data class Quiz(
    val question: String,
    val options: List<String>,
    val correct: String,
)