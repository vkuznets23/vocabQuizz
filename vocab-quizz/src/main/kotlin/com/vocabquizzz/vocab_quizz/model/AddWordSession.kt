package com.vocabquizzz.vocab_quizz.model

data class AddWordSession(
        var step: AddWordStep = AddWordStep.WORD,
        var word: String? = null,
        var definition: String? = null,
        var example: String? = null
    )