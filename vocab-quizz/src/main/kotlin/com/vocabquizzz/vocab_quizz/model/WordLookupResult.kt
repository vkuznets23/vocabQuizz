package com.vocabquizzz.vocab_quizz.model

import com.vocabquizzz.vocab_quizz.entity.Word
    
sealed interface WordLookupResult {
    data class Found(val word: Word) : WordLookupResult
    data class Suggestions(val query: String, val words: List<Word>) : WordLookupResult
    data object NotFound : WordLookupResult
}