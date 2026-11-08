package com.vocabquizzz.vocab_quizz.bot.handler

import org.springframework.stereotype.Component
import com.vocabquizzz.vocab_quizz.model.word.AddWordStep.*
import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import com.vocabquizzz.vocab_quizz.service.session.AddWordSessionService
import com.vocabquizzz.vocab_quizz.service.word.WordService
import com.vocabquizzz.vocab_quizz.service.user.UserService
import org.telegram.telegrambots.meta.api.objects.message.Message
import com.vocabquizzz.vocab_quizz.bot.formatter.WordLookupFormatter
import com.vocabquizzz.vocab_quizz.service.deck.DeckService
import com.vocabquizzz.vocab_quizz.entity.Deck
import com.vocabquizzz.vocab_quizz.entity.User

@Component
class AddWordHandler(
    private val sessionService: AddWordSessionService,
    private val wordService: WordService,
    private val userService: UserService,   
    private val deckService: DeckService,
    private val telegramGateway: TelegramGateway,
    private val wordLookupFormatter: WordLookupFormatter

) {
    private fun resolveDeck(user: User, text: String): Deck =
        if (text == "/skip") {
            deckService.getOrCreateGeneral(user)
        } else {
            deckService.getByNameForUser(user, text)
                ?: throw IllegalArgumentException("Deck not found. Use /decks.")
        }

    private fun sendValidationError(chatId: String, block: () -> Unit) {
        try {
            block()
        } catch (e: IllegalArgumentException) {
            telegramGateway.sendText(chatId, "❌ ${e.message}")
        }
    }

    
    fun start(message: Message) {
        val chatId = message.chatId.toString()
        val user = userService.getOrCreate(chatId.toLong())

        val word = message.text
            ?.substringAfter("/add")
            ?.trim()

        if (word.isNullOrBlank()) {
            telegramGateway.sendText(
                chatId,
                wordLookupFormatter.formatUsage("/add <word>")
            )
            return
        }

        sendValidationError(chatId) {
            wordService.validateWordInput(word)

            if (sessionService.start(chatId)) {
                sessionService.get(chatId)?.word = word
                telegramGateway.sendText(chatId, "Enter the definition:")
            } else {
                telegramGateway.sendText(chatId, "You're already adding a word 😊")
            }
        }

    }

    fun handle(message: Message) {
        val chatId = message.chatId.toString()
        val session = sessionService.get(chatId) ?: return

        when (session.step) {

            DEFINITION -> {
                val text = message.text?.trim()

                sendValidationError(chatId) {
                    wordService.validateDefinitionInput(text ?: "")
                    session.definition = text!!.trim()
                    session.step = EXAMPLE
                    telegramGateway.sendText(chatId, "Enter an example or type /skip:")
                }
            }

            EXAMPLE -> {
                val text = message.text?.trim()

                if (text.isNullOrBlank()) {
                    telegramGateway.sendText(chatId, "❌ Example cannot be empty. Enter an example or /skip.")
                    return
                }

                val example = if (text == "/skip") null else text

                sendValidationError(chatId) {
                    session.example = example
                    session.step = DECK
                    telegramGateway.sendText(chatId, "Enter the deck name or type /skip for General deck:")
                }
            }

            DECK -> {
                val text = message.text?.trim()
                if (text.isNullOrBlank()) {
                    telegramGateway.sendText(chatId, "❌ Enter deck name or /skip for General.")
                    return
                }
                val user = userService.getOrCreate(chatId.toLong())
                val deck = resolveDeck(user, text)

                sendValidationError(chatId) {
                    wordService.ensureWordAvailable(user, deck, session.word!!)
                    val saved = wordService.addWord(user, deck, session.word!!, session.definition!!, session.example)
                    sessionService.finish(chatId)
                    telegramGateway.sendText(chatId, "✅ «${saved.word}» added to «${deck.name}».")
                }
            }
        }
    }
}   