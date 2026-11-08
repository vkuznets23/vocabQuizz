package com.vocabquizzz.vocab_quizz.bot.formatter

import org.springframework.stereotype.Component
import com.vocabquizzz.vocab_quizz.entity.Deck

@Component
class DeckFormatter {
    fun formatDecks(decks: List<Deck>): String = buildString {
        appendLine("📁 Your decks:")
        if (decks.isEmpty()) {
            appendLine("No decks yet.")
            return@buildString
        }
        decks.forEach { appendLine("• ${it.name}") }
        appendLine()
        append("/words <name> — words in a deck")
        appendLine("/newdeck <name> — create")
    }
}