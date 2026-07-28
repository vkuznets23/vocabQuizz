package com.vocabquizzz.vocab_quizz.model

data class Word(
    val word: String,
    val correct: String,
    val options: List<String>
)