package com.vocabquizzz.vocab_quizz.bot.handler

import org.springframework.stereotype.Service
import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import com.vocabquizzz.vocab_quizz.service.WordService
import com.vocabquizzz.vocab_quizz.service.UserService
import org.telegram.telegrambots.meta.api.objects.message.Message

@Service
class DeleteWordHandler(
    private val wordService: WordService,
    private val userService: UserService,
    private val telegramGateway: TelegramGateway

) {
    fun handle(message: Message) {
        val chatId = message.chatId.toString()

        val word = message.text
            ?.substringAfter("/delete")
            ?.trim()

        if (word.isNullOrBlank()) {
            telegramGateway.sendText(
                chatId,
                "Usage: /delete <word>"
            )
            return
        }

        val user = userService.getOrCreate(chatId.toLong())
        val deleted = wordService.deleteWord(user, word)

        telegramGateway.sendText(
            chatId,
            if (deleted)
                "✅ '$word' deleted."
            else
                "❌ Word '$word' not found."
        )
    }
}