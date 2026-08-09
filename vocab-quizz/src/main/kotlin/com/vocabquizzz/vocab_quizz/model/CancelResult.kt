package com.vocabquizzz.vocab_quizz.model

sealed interface CancelResult {
    val message: String
    data class Cancelled(override val message: String) : CancelResult
    data class NothingToCancel(override val message: String = "Nothing to cancel.") : CancelResult
}