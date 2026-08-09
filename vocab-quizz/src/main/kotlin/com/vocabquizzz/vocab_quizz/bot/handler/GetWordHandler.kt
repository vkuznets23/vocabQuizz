package com.vocabquizzz.vocab_quizz.bot.handler

import org.springframework.stereotype.Component
import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import com.vocabquizzz.vocab_quizz.model.WordLookupResult
import com.vocabquizzz.vocab_quizz.service.WordService
import com.vocabquizzz.vocab_quizz.service.UserService
import org.telegram.telegrambots.meta.api.objects.message.Message
import com.vocabquizzz.vocab_quizz.bot.formatter.WordLookupFormatter

@Component
class GetWordHandler(
    private val wordService: WordService,
    private val userService: UserService,
    private val telegramGateway: TelegramGateway,
    private val wordLookupFormatter: WordLookupFormatter
) {
    
    fun handle(message: Message) {
        val chatId = message.chatId.toString()
        val user = userService.getOrCreate(chatId.toLong())

        val query = message.text
            ?.substringAfter("/word")
            ?.trim()

        if (query.isNullOrBlank()) {
            telegramGateway.sendText(
                chatId,
                wordLookupFormatter.formatUsage("/word <word>")
            )
            return
        }

        when (val result = wordService.lookupWord(user, query)) {
            is WordLookupResult.Found -> {
                telegramGateway.sendText(
                    chatId,
                    wordLookupFormatter.formatWord(result.word)
                )
            }
            is WordLookupResult.Suggestions -> {
                telegramGateway.sendText(chatId, wordLookupFormatter.formatSuggestions(result.query, result.words))
            }
            is WordLookupResult.NotFound -> {
                telegramGateway.sendText(
                    chatId,
                    wordLookupFormatter.formatNotFound(query)
                )
            }
        }
    }
}