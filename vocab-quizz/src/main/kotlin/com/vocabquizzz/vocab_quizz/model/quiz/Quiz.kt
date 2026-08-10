package com.vocabquizzz.vocab_quizz.model.quiz

import com.vocabquizzz.vocab_quizz.model.quiz.QuizType

data class Quiz(
    val question: String,
    val options: List<String>,
    val correct: String,
    val type: QuizType,
    val wordId: Long
)