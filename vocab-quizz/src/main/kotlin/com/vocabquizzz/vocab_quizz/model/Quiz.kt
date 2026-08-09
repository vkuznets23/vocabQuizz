package com.vocabquizzz.vocab_quizz.model

import com.vocabquizzz.vocab_quizz.model.QuizType

data class Quiz(
    val question: String,
    val options: List<String>,
    val correct: String,
    val type: QuizType,
    val wordId: Long
)