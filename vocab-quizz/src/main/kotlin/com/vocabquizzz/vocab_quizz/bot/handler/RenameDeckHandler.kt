package com.vocabquizzz.vocab_quizz.bot.handler

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.message.Message
import com.vocabquizzz.vocab_quizz.service.deck.DeckService
import com.vocabquizzz.vocab_quizz.service.user.UserService
import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import com.vocabquizzz.vocab_quizz.service.session.RenameDeckSessionService
import com.vocabquizzz.vocab_quizz.model.deck.RenameDeckStep
import com.vocabquizzz.vocab_quizz.model.deck.DeckDefaults
import com.vocabquizzz.vocab_quizz.entity.User

@Component
class RenameDeckHandler(
    private val deckService: DeckService,
    private val userService: UserService,
    private val telegramGateway: TelegramGateway,
    private val sessionService: RenameDeckSessionService
) {

    private fun resolveRenamableDeck(user: User, raw: String): String {
        val name = deckService.validateDeckName(raw)
        if (name.equals(DeckDefaults.GENERAL_NAME, ignoreCase = true)) {
            throw IllegalArgumentException("Cannot rename the default deck.")
        }
        deckService.getByNameForUser(user, name)
            ?: throw IllegalArgumentException("Deck not found. Use /decks.")
        return name
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

        val oldName = message.text
            ?.substringAfter("/renamedeck")
            ?.trim()

        if (oldName.isNullOrBlank()) {
            telegramGateway.sendText(chatId, "Usage: /renamedeck <old name>")
            return
        }

        if (sessionService.isRunning(chatId)) {
            telegramGateway.sendText(chatId, "You're already renaming a deck 😊")
            return
        }

        sendValidationError(chatId) {
            val validatedOldName = resolveRenamableDeck(user, oldName)
            if (!sessionService.start(chatId)) {
                telegramGateway.sendText(chatId, "You're already renaming a deck 😊")
                return@sendValidationError
            }
            sessionService.get(chatId)?.apply {
                this.oldName = validatedOldName
                step = RenameDeckStep.NEW_NAME
            }
            telegramGateway.sendText(chatId, "Enter the new name:")
        }
    }

    fun handle(message: Message) {
        val chatId = message.chatId.toString()
        val session = sessionService.get(chatId) ?: return

        when (session.step) {

            RenameDeckStep.OLD_NAME -> {
                sessionService.finish(chatId)
                telegramGateway.sendText(chatId, "Use /renamedeck <old name> to start.")
            }

            RenameDeckStep.NEW_NAME -> {
                val text = message.text?.trim()
                if (text.isNullOrBlank()) {
                    telegramGateway.sendText(chatId, "❌ Enter the new deck name.")
                    return
                }

                val user = userService.getOrCreate(chatId.toLong())
                sendValidationError(chatId) {
                    val newName = deckService.validateDeckName(text)
                    val oldName = session.oldName!!
                    val saved = deckService.renameDeck(user, oldName, newName)
                    sessionService.finish(chatId)
                    telegramGateway.sendText(chatId, "✅ Deck «$oldName» renamed to «${saved.name}».")
                }
            }
        }
    }
}