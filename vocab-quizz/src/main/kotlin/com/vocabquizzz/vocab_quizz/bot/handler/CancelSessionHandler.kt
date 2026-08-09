package com.vocabquizzz.vocab_quizz.bot.handler

import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import com.vocabquizzz.vocab_quizz.service.SessionCancelService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.message.Message

@Component
class CancelSessionHandler(
    private val sessionCancelService: SessionCancelService,
    private val telegramGateway: TelegramGateway,
) {
    fun handle(message: Message) {
        val chatId = message.chatId.toString()
        val result = sessionCancelService.cancelAll(chatId)
        telegramGateway.sendText(chatId, result.message)
    }
}