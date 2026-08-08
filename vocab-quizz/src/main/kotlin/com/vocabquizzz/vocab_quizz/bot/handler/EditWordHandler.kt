package com.vocabquizzz.vocab_quizz.bot.handler

import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import com.vocabquizzz.vocab_quizz.service.WordService
import com.vocabquizzz.vocab_quizz.service.UserService
import com.vocabquizzz.vocab_quizz.service.EditWordSessionService
import com.vocabquizzz.vocab_quizz.model.EditableField
import com.vocabquizzz.vocab_quizz.model.EditWordStep.*
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.message.Message

@Service
class EditWordHandler(
    private val sessionService: EditWordSessionService,
    private val wordService: WordService,
    private val userService: UserService,
    private val telegramGateway: TelegramGateway
)  {
    fun start(message: Message) {
        val chatId = message.chatId.toString()

        val wordText = message.text
            ?.substringAfter("/edit")
            ?.trim()

        if (wordText.isNullOrBlank()) {
            telegramGateway.sendText(
                chatId,
                "Usage: /edit <word>"
            )
            return
        }

        println("[$wordText]")

        val user = userService.getOrCreate(chatId.toLong())

        val wordEntity = wordService.getWord(user, wordText)
        if (wordEntity == null) {
            telegramGateway.sendText(chatId, "Word not found.")
            return
        }

        if (!sessionService.start(chatId)) {
            telegramGateway.sendText(
                chatId,
                "You're already changing a word 😊"
            )
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
                            "Current example: ${session.example}\nEnter the new example or /skip::"
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
                    val value =
                        if (
                            session.field == EditableField.EXAMPLE &&
                            message.text == "/skip"
                        ) null
                        else message.text.trim()

                    
                    try {
                        wordService.updateWord(
                            user = user,
                            wordId = session.wordId!!,
                            field = session.field!!,
                            value = value
                        )

                        sessionService.finish(chatId)

                        telegramGateway.sendText(
                            chatId,
                            "✅ Word updated!"
                        )
                    } catch (e: IllegalArgumentException) {
                        telegramGateway.sendText(
                            chatId,
                            "❌ ${e.message}"
                        )
                    }

                }
            }
        }
}