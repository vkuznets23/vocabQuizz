package com.vocabquizzz.vocab_quizz.bot.handler

import org.springframework.stereotype.Component
import com.vocabquizzz.vocab_quizz.service.user.UserService
import com.vocabquizzz.vocab_quizz.service.UserQuizSettingsService
import com.vocabquizzz.vocab_quizz.bot.formatter.SettingsFormatter
import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import org.telegram.telegrambots.meta.api.objects.message.Message  


@Component
class SettingsHandler(
    private val userService: UserService,
    private val settingsService: UserQuizSettingsService,
    private val formatter: SettingsFormatter,
    private val telegramGateway: TelegramGateway,
) {
    fun handle(message: Message) {
        val chatId = message.chatId.toString()
        val user = userService.getOrCreate(chatId.toLong())
        val settings = settingsService.getSettings(user)

        val (text, keyboard) = formatter.formatMain(settings)
        telegramGateway.sendTextWithKeyboard(chatId, text, keyboard)
    }

}