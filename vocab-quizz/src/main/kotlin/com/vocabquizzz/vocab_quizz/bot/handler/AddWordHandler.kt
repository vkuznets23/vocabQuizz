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
                session.definition = message.text
                session.step = EXAMPLE

                telegramGateway.sendText(
                    chatId,
                    "Enter an example or type /skip:"
                )
            }

            EXAMPLE -> {
                session.example =
                    if (message.text == "/skip") null
                    else message.text

                val user = userService.getOrCreate(chatId.toLong())

                try {
                    wordService.addWord(
                        user = user, word = session.word!!, definition = session.definition!!, example = session.example
                    )
                    telegramGateway.sendText(chatId, "✅ Word added!")
                } catch (e: IllegalArgumentException) {
                    telegramGateway.sendText(chatId, "You already have this word.")
                } finally {
                    sessionService.finish(chatId)
                }

                sessionService.finish(chatId)
            }
        }
    }
}   