package com.vocabquizzz.vocab_quizz.model.session
import com.vocabquizzz.vocab_quizz.model.deck.RenameDeckStep

data class RenameDeckSession(
    var step: RenameDeckStep = RenameDeckStep.OLD_NAME,
    var oldName: String? = null,
)
