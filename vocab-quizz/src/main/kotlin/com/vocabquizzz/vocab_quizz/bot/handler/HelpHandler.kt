package com.vocabquizzz.vocab_quizz.bot.handler

import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.message.Message
import com.vocabquizzz.vocab_quizz.bot.formatter.HelpFormatter

@Component
class HelpHandler(
    private val telegramGateway: TelegramGateway,
    private val helpFormatter: HelpFormatter
) {
    fun handle(message: Message) {
        val chatId = message.chatId.toString()

        telegramGateway.sendText(
            chatId,
            helpFormatter.formatHelp()
        )
    }
}