package com.vocabquizzz.vocab_quizz.bot.handler

import org.springframework.stereotype.Service
import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import com.vocabquizzz.vocab_quizz.service.WordService
import com.vocabquizzz.vocab_quizz.service.UserService
import org.telegram.telegrambots.meta.api.objects.message.Message

@Service
class GetWordHandler(
    private val wordService: WordService,
    private val userService: UserService,
    private val telegramGateway: TelegramGateway

) {
    fun handle(message: Message) {
        val chatId = message.chatId.toString()

        val query = message.text
            ?.substringAfter("/word")
            ?.trim()

        if (query.isNullOrBlank()) {
            telegramGateway.sendText(
                chatId,
                "Usage: /word <word>"
            )
            return
        }

        val user = userService.getOrCreate(chatId.toLong())
        val exact = wordService.getWord(user, query)

        if (exact == null) {
            val suggestions =
                wordService.searchWords(user, query)

            if (suggestions.isEmpty()) {
                telegramGateway.sendText(
                    chatId,
                    "❌ Word '$query' not found."
                )
                return
            }

            val text = buildString {
                appendLine("❌ Word '$query' not found.")
                appendLine()
                appendLine("Did you mean:")
                appendLine()

                suggestions.forEach {
                    appendLine("• ${it.word}")
                }
            }

            telegramGateway.sendText(
                chatId,
                text
            )

            return
        }

        telegramGateway.sendText(
            chatId,
            """
            📖 ${exact.word}

            📚 Definition:
            ${exact.definition}

            💬 Example:
            ${exact.example ?: "-"}
            """.trimIndent()
        )
    }
}