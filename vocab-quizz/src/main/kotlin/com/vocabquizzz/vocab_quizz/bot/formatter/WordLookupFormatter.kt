package com.vocabquizzz.vocab_quizz.bot.formatter

import org.springframework.stereotype.Component
import com.vocabquizzz.vocab_quizz.entity.Word

@Component
class WordLookupFormatter {
    fun formatWord(word: Word) = """
        📖 ${word.word}

        📚 Definition:
        ${word.definition}

        💬 Example:
        ${word.example ?: "-"}
    """.trimIndent()

    fun formatSuggestions(query: String, words: List<Word>) = buildString {
        appendLine("❌ Word '$query' not found.")
        appendLine()
        appendLine("Did you mean:")
        appendLine()

        words.forEach {
            appendLine("• ${it.word}")
        }
    }

    fun formatNotFound(query: String): String = "❌ Word '$query' not found."

    fun formatUsage(command: String) = "Usage: $command"
}