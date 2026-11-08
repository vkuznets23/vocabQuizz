package com.vocabquizzz.vocab_quizz.bot.handler

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.message.Message
import com.vocabquizzz.vocab_quizz.service.deck.DeckService
import com.vocabquizzz.vocab_quizz.service.user.UserService
import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway

@Component
class DeleteDeckHandler(
    private val deckService: DeckService,
    private val userService: UserService,
    private val telegramGateway: TelegramGateway
) {
    fun handle(message: Message) {
        val chatId = message.chatId.toString()
        val user = userService.getOrCreate(chatId.toLong())

        val deckName = message.text
            ?.substringAfter("/deletedeck")
            ?.trim()
            ?.takeIf { it.isNotBlank() }

        if (deckName == null) {
            telegramGateway.sendText(chatId, "Usage: /deletedeck <name>")
            return
        }

        try {
            deckService.deleteDeck(user, deckName)
            telegramGateway.sendText(chatId, "✅ Deck «${deckName}» deleted.")

        } catch (e: IllegalArgumentException) {
            telegramGateway.sendText(chatId, "❌ ${e.message}")
        }
    }
}