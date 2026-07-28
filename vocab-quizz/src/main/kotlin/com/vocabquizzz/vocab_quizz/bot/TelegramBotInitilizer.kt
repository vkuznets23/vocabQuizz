package com.vocabquizzz.vocab_quizz.bot

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@Component
class TelegramBotInitializer(
    private val telegramBot: TelegramBot
) {

    @PostConstruct
    fun init() {
        val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
        botsApi.registerBot(telegramBot)

        println("🔥 Telegram bot started")
    }
}