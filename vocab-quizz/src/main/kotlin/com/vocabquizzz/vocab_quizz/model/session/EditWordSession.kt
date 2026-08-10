package com.vocabquizzz.vocab_quizz.model.session

import com.vocabquizzz.vocab_quizz.model.word.EditableField
import com.vocabquizzz.vocab_quizz.model.word.EditWordStep

data class EditWordSession(
    var wordId: Long? = null,
    var word: String? = null,
    var definition: String? = null,
    var example: String? = null,
    var field: EditableField? = null,
    var step: EditWordStep = EditWordStep.SELECT_FIELD
)