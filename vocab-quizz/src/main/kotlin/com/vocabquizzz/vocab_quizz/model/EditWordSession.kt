package com.vocabquizzz.vocab_quizz.model

data class EditWordSession(
    var wordId: Long? = null,
    var word: String? = null,
    var definition: String? = null,
    var example: String? = null,
    var field: EditableField? = null,
    var step: EditWordStep = EditWordStep.SELECT_FIELD
)