package com.vocabquizzz.vocab_quizz.bot.handler

import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.message.Message
import com.vocabquizzz.vocab_quizz.model.WordConstraints

@Component
class HelpHandler(
    private val telegramGateway: TelegramGateway
) {
    fun handle(message: Message) {
        val chatId = message.chatId.toString()

        telegramGateway.sendText(
            chatId,
            """
            🤖 Vocab Quiz Bot

            📚 Vocabulary

            📝 /add <word>
            Add a word. I'll ask for definition, then example (or /skip).

            ✏️ /edit <word>
            Edit word, definition, or example. Choose: 1/word, 2/definition, 3/example.
            For example use /skip to clear it.

            🗑️ /delete <word>
            Delete a word from your vocabulary.

            📖 /word <word>
            Show definition and example. Suggests similar words if not found.

            📚 /words
            List your words (paginated). Use ⬅️ ➡️ buttons to navigate.

            🧠 Quiz

            /quiz
            Start a quiz: '${WordConstraints.QUESTIONS_PER_LESSON}' questions, different question types.
            You need at least '${WordConstraints.MIN_WORDS_FOR_QUIZ}' words in your dictionary.
            Answer using the poll buttons Telegram sends you.

            📊 Statistics

            /stat
            Your overall quiz stats (words practiced, accuracy, etc.).

            /stat <word>
            Stats for a specific word.

            ⚙️ Other

            ❌ /cancel
            Cancel current add / edit / quiz session.

            ❓ /help
            Show this message.

            💡 Tips
            • Example sentences: replace the word with ___
            e.g. I ate an ___ for breakfast.
            • Word max 50 chars, definition/example max 200 chars.
            """.trimIndent()
        )

        return
    }
}