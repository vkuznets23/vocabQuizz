package com.vocabquizzz.vocab_quizz.model

data class AddWordSession(
        var step: AddWordStep = AddWordStep.DEFINITION,
        var word: String? = null,
        var definition: String? = null,
        var example: String? = null
    )