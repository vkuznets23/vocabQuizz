package com.vocabquizzz.vocab_quizz.model.word

sealed interface DeleteWordResult {
    data object Deleted : DeleteWordResult
    data object NotFound : DeleteWordResult
}