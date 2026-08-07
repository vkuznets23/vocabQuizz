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

        val wordText = message.text
            ?.substringAfter("/word")
            ?.trim()

        if (wordText.isNullOrBlank()) {
            telegramGateway.sendText(
                chatId,
                "Usage: /word <word>"
            )
            return
        }

        val user = userService.getOrCreate(chatId.toLong())
        val word = wordService.getWord(user, wordText)

        if (word == null) {
            telegramGateway.sendText(
                chatId,
                "❌ Word '$wordText' not found."
            )
            return
        }

        telegramGateway.sendText(
            chatId,
            """
            📖 ${word.word}

            📚 Definition:
            ${word.definition}

            💬 Example:
            ${word.example ?: "-"}
            """.trimIndent()
        )
    }
}