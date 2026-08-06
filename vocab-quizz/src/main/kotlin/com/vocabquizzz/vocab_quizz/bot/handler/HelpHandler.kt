package com.vocabquizzz.vocab_quizz.bot.handler

import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.message.Message

@Service
class HelpHandler(
    private val telegramGateway: TelegramGateway
) {
    fun handle(message: Message) {
        val chatId = message.chatId.toString()

        telegramGateway.sendText(
            chatId,
            """
            🤖 Vocab Quiz Bot

            Here's what I can do:

            📝 /add <word>
            Add a new word to your vocabulary.
            I'll ask you for its definition and an optional example.

            💡 If you add an example, replace the word with "___".
            For example:
            I ate an ___ for breakfast.

            ✏️ /edit <word>
            Edit a word, its definition, or its example.

            🗑️ /delete <word>
            Delete a word from your vocabulary.

            🧠 /quiz
            Start a quiz using your saved words.

            📚 /words
            Show all the words you've added to your vocabulary.

            💡 Tip:
            Use /help anytime to see this list again.
            """.trimIndent()
        )

        return
    }
}