package com.vocabquizzz.vocab_quizz.model

sealed interface DeleteWordResult {
    data object Deleted : DeleteWordResult
    data object NotFound : DeleteWordResult
}