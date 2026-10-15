package com.vocabquizzz.vocab_quizz.bot.handler

import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.message.Message
import com.vocabquizzz.vocab_quizz.bot.formatter.HelpFormatter
import com.vocabquizzz.vocab_quizz.service.UserQuizSettingsService
import com.vocabquizzz.vocab_quizz.service.user.UserService
@Component
class HelpHandler(
    private val telegramGateway: TelegramGateway,
    private val helpFormatter: HelpFormatter,
    private val userService: UserService,
    private val settingsService: UserQuizSettingsService,
) {
    fun handle(message: Message) {
        val chatId = message.chatId.toString()
        val user = userService.getOrCreate(chatId.toLong())
        val settings = settingsService.getSettings(user)

        telegramGateway.sendText(
            chatId,
            helpFormatter.formatHelp(settings.questionsPerLesson)
        )
    }
}