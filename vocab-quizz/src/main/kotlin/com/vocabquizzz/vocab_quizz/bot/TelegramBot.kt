package com.vocabquizzz.vocab_quizz.bot

import com.vocabquizzz.vocab_quizz.service.BotService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer
import org.telegram.telegrambots.meta.api.objects.Update


@Component
class TelegramBot(
    @Value("\${telegram.bot.token}")
    private val token: String,

    private val botService: BotService

) : SpringLongPollingBot,
    LongPollingSingleThreadUpdateConsumer {

    override fun getBotToken(): String {
        return token
    }

    override fun getUpdatesConsumer(): LongPollingUpdateConsumer {
        return this
    }

    override fun consume(update: Update) {

        println("Telegram update received")

        if (update.hasMessage() && update.message.hasText()) {
            botService.handleMessage(update.message)
        }

        if (update.hasPollAnswer()) {
            botService.handlePollAnswer(update.pollAnswer)
        }
    }
}