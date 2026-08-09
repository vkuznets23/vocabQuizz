package com.vocabquizzz.vocab_quizz.bot.handler

import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import com.vocabquizzz.vocab_quizz.service.UserService 
import com.vocabquizzz.vocab_quizz.service.StatisticService 
import com.vocabquizzz.vocab_quizz.service.WordService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.message.Message
import com.vocabquizzz.vocab_quizz.model.WordLookupResult
import com.vocabquizzz.vocab_quizz.bot.formatter.WordLookupFormatter
import com.vocabquizzz.vocab_quizz.bot.formatter.StatisticFormatter

@Component
class StatisticHandler(
    private val telegramGateway: TelegramGateway,
    private val statisticService: StatisticService,
    private val userService: UserService, 
    private val wordService: WordService,
    private val wordLookupFormatter: WordLookupFormatter,
    private val statisticFormatter: StatisticFormatter
) {

    fun handle(message: Message) {  
        val chatId = message.chatId.toString()
        val user = userService.getOrCreate(chatId.toLong())

        val query = message.text
            ?.substringAfter("/stat")
            ?.trim()

        if (query.isNullOrBlank()) {
            val stat = statisticService.getUserStatistic(user)
            telegramGateway.sendText(chatId, statisticFormatter.formatUserStatistic(stat))
            return
        }

        when (val lookup = wordService.lookupWord(user, query)) {
            is WordLookupResult.Found -> {
                val stat = statisticService.getStatisticForWord(lookup.word)
                telegramGateway.sendText(chatId, statisticFormatter.formatWordStatistic(stat))
            }
            is WordLookupResult.Suggestions ->
                telegramGateway.sendText(chatId, wordLookupFormatter.formatSuggestions(lookup.query, lookup.words))
            WordLookupResult.NotFound ->
                telegramGateway.sendText(chatId, wordLookupFormatter.formatNotFound(query))
        }
    }
}