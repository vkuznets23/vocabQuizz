package com.vocabquizzz.vocab_quizz.model

data class Quiz(
    val word: String,
    val correct: String,
    val options: List<String>
)