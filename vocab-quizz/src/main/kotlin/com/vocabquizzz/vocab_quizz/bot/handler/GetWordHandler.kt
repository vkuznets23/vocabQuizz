package com.vocabquizzz.vocab_quizz.bot.handler

import org.springframework.stereotype.Component
import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import com.vocabquizzz.vocab_quizz.entity.Word
import com.vocabquizzz.vocab_quizz.model.WordLookupResult
import com.vocabquizzz.vocab_quizz.service.WordService
import com.vocabquizzz.vocab_quizz.service.UserService
import org.telegram.telegrambots.meta.api.objects.message.Message

@Component
class GetWordHandler(
    private val wordService: WordService,
    private val userService: UserService,
    private val telegramGateway: TelegramGateway

) {
    private fun formatWord(word: Word) = """
        📖 ${word.word}

        📚 Definition:
        ${word.definition}

        💬 Example:
        ${word.example ?: "-"}
    """.trimIndent()
    
    private fun formatSuggestions(query: String, words: List<Word>) = buildString {
        appendLine("❌ Word '$query' not found.")
        appendLine()
        appendLine("Did you mean:")
        appendLine()

        words.forEach {
            appendLine("• ${it.word}")
        }
    }

    fun handle(message: Message) {
        val chatId = message.chatId.toString()
        val user = userService.getOrCreate(chatId.toLong())

        val query = message.text
            ?.substringAfter("/word")
            ?.trim()

        if (query.isNullOrBlank()) {
            telegramGateway.sendText(
                chatId,
                "Usage: /word <word>"
            )
            return
        }

        when (val result = wordService.lookupWord(user, query)) {
            is WordLookupResult.Found -> {
                telegramGateway.sendText(
                    chatId,
                    formatWord(result.word)
                )
            }
            is WordLookupResult.Suggestions -> {
                telegramGateway.sendText(chatId, formatSuggestions(result.query, result.words))
            }
            is WordLookupResult.NotFound -> {
                telegramGateway.sendText(
                    chatId,
                    "❌ Word '$query' not found."
                )
            }
        }
    }
}