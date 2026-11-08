package com.vocabquizzz.vocab_quizz.bot.handler

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.message.Message
import com.vocabquizzz.vocab_quizz.service.deck.DeckService
import com.vocabquizzz.vocab_quizz.service.user.UserService
import com.vocabquizzz.vocab_quizz.bot.formatter.DeckFormatter
import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway

@Component
class DeckHandler(
    private val deckService: DeckService,
    private val userService: UserService,
    private val deckFormatter: DeckFormatter,
    private val telegramGateway: TelegramGateway
) {

    fun handle(message: Message) {
        val chatId = message.chatId.toString()
        val user = userService.getOrCreate(chatId.toLong())

        var decks = deckService.getAllDecks(user)
        if (decks.isEmpty()) {
            deckService.getOrCreateGeneral(user)
            decks = deckService.getAllDecks(user)
        }


        telegramGateway.sendText(chatId, deckFormatter.formatDecks(decks))
    }
}