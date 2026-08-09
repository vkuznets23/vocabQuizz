package com.vocabquizzz.vocab_quizz.bot.handler

import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import com.vocabquizzz.vocab_quizz.service.UserService 
import com.vocabquizzz.vocab_quizz.service.StatisticService 
import com.vocabquizzz.vocab_quizz.service.WordService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.message.Message
import com.vocabquizzz.vocab_quizz.model.UserStatistic
import com.vocabquizzz.vocab_quizz.model.WordStatistic
import com.vocabquizzz.vocab_quizz.model.WordLookupResult
import com.vocabquizzz.vocab_quizz.entity.Word

@Component
class StatisticHandler(
    private val telegramGateway: TelegramGateway,
    private val statisticService: StatisticService,
    private val userService: UserService, 
    private val wordService: WordService
) {

    private fun formatUserStatistic(stat: UserStatistic) = """
        📊 Your statistics
        Words in dictionary: ${stat.totalWords}
        Words practiced: ${stat.wordsAttempted}
        Total attempts: ${stat.totalAttempts}
        Correct: ${stat.correctAnswers}
        Wrong: ${stat.wrongAnswers}
        Accuracy: ${"%.2f".format(stat.accuracy)}%
    """.trimIndent()

    private fun formatWordStatistic(stat: WordStatistic) = """
        📊 Statistics for "${stat.word}"
        Attempts: ${stat.totalAttempts}
        Correct: ${stat.correctAnswers}
        Wrong: ${stat.wrongAnswers}
        Accuracy: ${"%.2f".format(stat.accuracy)}%
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
            ?.substringAfter("/stat")
            ?.trim()

        if (query.isNullOrBlank()) {
            val stat = statisticService.getUserStatistic(user)
            telegramGateway.sendText(chatId, formatUserStatistic(stat))
            return
        }

        when (val lookup = wordService.lookupWord(user, query)) {
            is WordLookupResult.Found -> {
                val stat = statisticService.getStatisticForWord(lookup.word)
                telegramGateway.sendText(chatId, formatWordStatistic(stat))
            }
            is WordLookupResult.Suggestions ->
                telegramGateway.sendText(chatId, formatSuggestions(lookup.query, lookup.words))
            WordLookupResult.NotFound ->
                telegramGateway.sendText(chatId, "❌ Word '$query' not found.")
        }
    }
}