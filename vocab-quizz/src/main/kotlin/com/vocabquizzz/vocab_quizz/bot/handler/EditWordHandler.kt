package com.vocabquizzz.vocab_quizz.bot.handler

import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import com.vocabquizzz.vocab_quizz.service.WordService
import com.vocabquizzz.vocab_quizz.service.UserService
import com.vocabquizzz.vocab_quizz.service.EditWordSessionService
import com.vocabquizzz.vocab_quizz.model.EditableField
import com.vocabquizzz.vocab_quizz.model.EditWordStep.*
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.message.Message
import com.vocabquizzz.vocab_quizz.bot.formatter.WordLookupFormatter

@Component
class EditWordHandler(
    private val sessionService: EditWordSessionService,
    private val wordService: WordService,
    private val userService: UserService,
    private val telegramGateway: TelegramGateway,
    private val wordLookupFormatter: WordLookupFormatter
)  {

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

        val wordText = message.text
            ?.substringAfter("/edit")
            ?.trim()

        if (wordText.isNullOrBlank()) {
            telegramGateway.sendText(
                chatId,
                wordLookupFormatter.formatUsage("/edit <word>")
            )
            return
        }

        val wordEntity = wordService.getWord(user, wordText)
        if (wordEntity == null) {
            telegramGateway.sendText(chatId, "❌ Word not found.")
            return
        }
        if (!sessionService.start(chatId)) {
            telegramGateway.sendText(chatId, "You're already editing a word 😊")
            return
        }

        val session = sessionService.get(chatId) ?: return
        session.wordId = wordEntity.id
        session.word = wordEntity.word
        session.definition = wordEntity.definition
        session.example = wordEntity.example

        telegramGateway.sendText(
            chatId,
            """
            Editing "${wordEntity.word}"

            Word: ${wordEntity.word}
            Definition: ${wordEntity.definition}
            Example: ${wordEntity.example ?: "-"}
            
            What do you want to edit?
            1. word
            2. definition
            3. example
            """.trimIndent()
        )

        session.step = SELECT_FIELD
    }

    fun handle(message: Message) {
        val chatId = message.chatId.toString()
        val user = userService.getOrCreate(chatId.toLong())
        val session = sessionService.get(chatId) ?: return

        when (session.step) {
            SELECT_FIELD -> {
                when (message.text?.trim()?.lowercase()) {
                    "1", "word" -> {
                        session.field = EditableField.WORD
                        session.step = ENTER_VALUE

                        telegramGateway.sendText(
                            chatId,
                            "Current word: ${session.word}\nEnter the new word:"
                        )
                    }

                    "2", "definition" -> {
                        session.field = EditableField.DEFINITION
                        session.step = ENTER_VALUE

                        telegramGateway.sendText(
                            chatId,
                            "Current definition: ${session.definition}\nEnter the new definition:"
                        )
                    }

                    "3", "example" -> {
                        session.field = EditableField.EXAMPLE
                        session.step = ENTER_VALUE

                        telegramGateway.sendText(
                            chatId,
                            "Current example: ${session.example ?: "-"}\nEnter the new example or /skip:"
                        )
                    }

                    else -> {
                        telegramGateway.sendText(
                            chatId,
                            "Please choose: word, definition or example OR 1, 2, 3"
                        )
                    }
                }
            }

            ENTER_VALUE -> {
                    val text = message.text?.trim()

                    if (session.field == EditableField.EXAMPLE && text.isNullOrBlank()) {
                        telegramGateway.sendText(chatId, "❌ Enter an example or type /skip.")
                        return
                    }

                    val value = when {
                        session.field == EditableField.EXAMPLE && text == "/skip" -> null
                        else -> text
                    }

                    sendValidationError(chatId) {
                        wordService.updateWord(
                            user = user,
                            wordId = session.wordId!!,
                            field = session.field!!,
                            value = value
                        )
                        sessionService.finish(chatId)
                        telegramGateway.sendText(chatId, "✅ Word updated!")
                    }
                }
            }
        }
}