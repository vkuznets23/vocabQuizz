package com.vocabquizzz.vocab_quizz.bot.handler

import org.springframework.stereotype.Component
import com.vocabquizzz.vocab_quizz.service.user.UserService
import com.vocabquizzz.vocab_quizz.service.UserQuizSettingsService
import com.vocabquizzz.vocab_quizz.bot.formatter.SettingsFormatter
import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

@Component
class SettingsCallbackHandler(
    private val userService: UserService,
    private val settingsService: UserQuizSettingsService,
    private val formatter: SettingsFormatter,
    private val telegramGateway: TelegramGateway,
) {
    fun handle(callbackQuery: CallbackQuery) {
        val chatId = callbackQuery.message.chatId.toString()
        val messageId = callbackQuery.message.messageId
        val user = userService.getOrCreate(chatId.toLong())
        val settings = settingsService.getSettings(user)

        when (val action = callbackQuery.data.removePrefix("settings:")) {
            "toggle" -> settingsService.setEnabled(user, !settings.enabled)

            else -> when {
                action.startsWith("interval:") -> {
                    val hours = action.removePrefix("interval:").toIntOrNull() ?: return
                    settingsService.setInterval(user, hours)
                }
                action.startsWith("questionsPerLesson:") -> {
                    val count = action.removePrefix("questionsPerLesson:").toIntOrNull() ?: return
                    settingsService.setQuestionsPerLesson(user, count)
                }
                action.startsWith("window:") -> {
                    val preset = action.removePrefix("window:")
                    settingsService.setWindowPreset(user, preset)
                }
            }
        }

        val updated = settingsService.getSettings(user)  // after changes!
        val (text, keyboard) = formatter.formatMain(updated)

        telegramGateway.editMessage(chatId, messageId, text, keyboard)
        telegramGateway.answerCallback(callbackQuery.id)
    }
}