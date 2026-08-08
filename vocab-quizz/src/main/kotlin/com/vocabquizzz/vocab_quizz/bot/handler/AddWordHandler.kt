package com.vocabquizzz.vocab_quizz.bot.handler

import com.vocabquizzz.vocab_quizz.model.AddWordStep.*
import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import com.vocabquizzz.vocab_quizz.service.AddWordSessionService
import com.vocabquizzz.vocab_quizz.service.WordService
import com.vocabquizzz.vocab_quizz.service.UserService
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.message.Message

@Service
class AddWordHandler(
    private val sessionService: AddWordSessionService,
    private val wordService: WordService,
    private val userService: UserService,
    private val telegramGateway: TelegramGateway

) {
    fun start(message: Message) {
        val chatId = message.chatId.toString()
        val user = userService.getOrCreate(chatId.toLong())

        val word = message.text
            ?.substringAfter("/add")
            ?.trim()

        if (word.isNullOrBlank()) {
            telegramGateway.sendText(
                chatId,
                "Usage: /add <word>"
            )
            return
        }

        if (word.length > 100) {
            telegramGateway.sendText(
                chatId,
                "❌ Word is too long. Maximum is 100 characters."
            )
            return
        }

        if (wordService.getWord(user, word) != null) {
            telegramGateway.sendText(
                chatId,
                "❌ You already have the word '$word'."
            )
            return
        }

        if (!sessionService.start(chatId)) {
            telegramGateway.sendText(
                chatId,
                "You're already adding a word 😊"
            )
            return
        }

        val session = sessionService.get(chatId) ?: return

        session.word = word
        telegramGateway.sendText(
            chatId,
            "Enter the definition:"
        )

    }

    fun handle(message: Message) {
        val chatId = message.chatId.toString()
        val session = sessionService.get(chatId) ?: return

        when (session.step) {

            DEFINITION -> {
                val text = message.text?.trim()

                if (text.isNullOrBlank()) {
                    telegramGateway.sendText(chatId, "❌ Definition cannot be empty.")
                    return
                }

                if (text.length > 1000) {
                    telegramGateway.sendText(
                        chatId,
                        "❌ Definition is too long. Maximum is 1000 characters."
                    )
                    return
                }

                session.definition = text
                session.step = EXAMPLE

                telegramGateway.sendText(
                    chatId,
                    "Enter an example or type /skip:"
                )
            }

            EXAMPLE -> {
                val text = message.text?.trim()

                if (text.isNullOrBlank()) {
                    telegramGateway.sendText(chatId, "❌ Example cannot be empty. Enter an example or /skip.")
                    return
                }

                if (text.length > 2000) {
                    telegramGateway.sendText(
                        chatId,
                        "❌ Example is too long. Maximum is 2000 characters."
                    )
                    return
                }

                session.example =
                    if (text == "/skip") null
                    else text

                val user = userService.getOrCreate(chatId.toLong())

                try {
                    wordService.addWord(
                        user = user, word = session.word!!, 
                        definition = session.definition!!, 
                        example = session.example
                    )
                    telegramGateway.sendText(chatId, "✅ Word added!")
                } catch (e: IllegalArgumentException) {
                    telegramGateway.sendText(
                        chatId,
                        "❌ ${e.message}"
                    )
                } finally {
                    sessionService.finish(chatId)
                }
            }
        }
    }
}   