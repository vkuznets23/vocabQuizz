package com.vocabquizzz.vocab_quizz.bot.formatter

import org.springframework.stereotype.Component

@Component
class StartFormatter {
    fun formatStart() =
            """
    👋 Welcome to Vocab Quiz!
    Your personal dictionary + quizzes in Telegram.
    Quick start: /add → add 4+ words → /quiz
    or start with /help to see the commands list.

    ---
    """.trimIndent()
}
