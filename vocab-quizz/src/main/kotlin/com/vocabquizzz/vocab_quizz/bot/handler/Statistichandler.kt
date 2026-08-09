package com.vocabquizzz.vocab_quizz.bot.handler

import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import com.vocabquizzz.vocab_quizz.service.UserService 
import com.vocabquizzz.vocab_quizz.service.WordAttemptService 
import com.vocabquizzz.vocab_quizz.service.WordService
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.message.Message

@Service
class StatisticHandler(
    private val telegramGateway: TelegramGateway,
    private val statisticService: WordAttemptService,
    private val userService: UserService, 
    private val wordService: WordService
) {
    fun handle(message: Message) {
        val chatId = message.chatId.toString()
        val user = userService.getOrCreate(chatId.toLong())

        val word = message.text
            ?.substringAfter("/stat")
            ?.trim()

        if (word.isNullOrBlank()) {
                // /stat
                val statistic = statisticService.getUserStatistic(user)

                telegramGateway.sendText( chatId, 
                    """ 
                    📊 Your statistics 

                    Words in dictionary: ${statistic.totalWords} 
                    Words practiced: ${statistic.wordsAttempted} 
                    Total attempts: ${statistic.totalAttempts} 
                    Correct: ${statistic.correctAnswers} 
                    Wrong: ${statistic.wrongAnswers} 
                    Accuracy: ${"%.2f".format(statistic.accuracy)}% 
                    """.trimIndent() 
                )

                return 
        }
         
        try {
                // /stat <word>
                val statistic = statisticService.getStatisticForWord(user, word)

                telegramGateway.sendText( 
                    chatId, 
                    """ 
                    📊 Statistics for "${statistic.word}" 

                    Attempts: ${statistic.totalAttempts} 
                    Correct: ${statistic.correctAnswers} 
                    Wrong: ${statistic.wrongAnswers} 
                    Accuracy: ${"%.2f".format(statistic.accuracy)}% 
                    """.trimIndent() 
                )
        } catch (e: IllegalArgumentException) {
            telegramGateway.sendText(
                chatId,
                "❌ You don't have such word: '$word'."
            )
        }


    }
}