package com.vocabquizzz.vocab_quizz.bot.handler

import com.vocabquizzz.vocab_quizz.bot.formatter.StartFormatter
import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import com.vocabquizzz.vocab_quizz.service.user.UserService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.message.Message

@Component
class StartHandler(
        private val telegramGateway: TelegramGateway,
        private val startFormatter: StartFormatter,
        private val userService: UserService,
) {
    fun handle(message: Message) {
        val chatId = message.chatId.toString()
        userService.getOrCreate(chatId.toLong()) // register on /start
        telegramGateway.sendText(chatId, startFormatter.formatStart())
    }
}
