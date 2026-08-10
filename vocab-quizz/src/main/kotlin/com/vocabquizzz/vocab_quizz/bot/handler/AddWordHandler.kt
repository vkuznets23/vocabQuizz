package com.vocabquizzz.vocab_quizz.bot.handler

import org.springframework.stereotype.Component
import com.vocabquizzz.vocab_quizz.model.word.AddWordStep.*
import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import com.vocabquizzz.vocab_quizz.service.session.AddWordSessionService
import com.vocabquizzz.vocab_quizz.service.word.WordService
import com.vocabquizzz.vocab_quizz.service.user.UserService
import org.telegram.telegrambots.meta.api.objects.message.Message
import com.vocabquizzz.vocab_quizz.bot.formatter.WordLookupFormatter

@Component
class AddWordHandler(
    private val sessionService: AddWordSessionService,
    private val wordService: WordService,
    private val userService: UserService,   
    private val telegramGateway: TelegramGateway,
    private val wordLookupFormatter: WordLookupFormatter

) {
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
            wordService.ensureWordAvailable(user, word)

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
                val user = userService.getOrCreate(chatId.toLong())

                sendValidationError(chatId) {
                    wordService.addWord(
                        user = user,
                        word = session.word!!,
                        definition = session.definition!!,
                        example = example
                    )
                    sessionService.finish(chatId)
                    telegramGateway.sendText(chatId, "✅ Word added!")
                }
            }
        }
    }
}   