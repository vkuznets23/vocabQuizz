package com.vocabquizzz.vocab_quizz.bot.handler

import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import com.vocabquizzz.vocab_quizz.service.word.WordService
import com.vocabquizzz.vocab_quizz.service.user.UserService
import com.vocabquizzz.vocab_quizz.service.deck.DeckService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.message.Message
import com.vocabquizzz.vocab_quizz.bot.formatter.AllWordsListFormatter

@Component
class AllWordsHandler(
    private val wordService: WordService,
    private val userService: UserService,
    private val deckService: DeckService,
    private val allWordsListFormatter: AllWordsListFormatter,
    private val telegramGateway: TelegramGateway
) {

    fun handle(message: Message) {
        val chatId = message.chatId.toString()
        val user = userService.getOrCreate(chatId.toLong())

        val deckName = message.text
            ?.substringAfter("/words")
            ?.trim()
            ?.takeIf { it.isNotBlank() }

        val page = if (deckName != null) {
            val deck = deckService.getByNameForUser(user, deckName)
                ?: run {
                    telegramGateway.sendText(
                        chatId,
                        "Deck «$deckName» not found. Use /decks.",
                    )
                    return
                }
            wordService.getWordsForDeck(deck, 0)
        } else {
            wordService.getWords(user, 0)
        }

        val (text, keyboard) = allWordsListFormatter.formatPage(page)

        telegramGateway.sendTextWithKeyboard(
            chatId,
            text,
            keyboard
        )
    }
}