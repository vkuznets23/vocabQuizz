package com.vocabquizzz.vocab_quizz.bot.gateway

import com.vocabquizzz.vocab_quizz.model.quiz.Quiz
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.generics.TelegramClient
import org.telegram.telegrambots.meta.api.objects.polls.input.InputPollOption
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery


@Component
class TelegramGateway(
    private val telegramClient: TelegramClient
) {

    fun sendText(chatId: String, text: String ) {
        telegramClient.execute(
            SendMessage(chatId, text)
        )
    }

    fun sendTextWithKeyboard(chatId: String, text: String, keyboard: InlineKeyboardMarkup) {
        val message = SendMessage.builder()
            .chatId(chatId)
            .text(text)
            .replyMarkup(keyboard)
            .build()

        telegramClient.execute(message)
    }

    fun sendQuiz(chatId: String, quiz: Quiz): String {

        val correctOption = quiz.options.indexOf(quiz.correct)

        val poll = SendPoll.builder()
            .chatId(chatId)
            .question(quiz.question)
            .options(
                quiz.options.map {
                    InputPollOption(it)
                }
            )
            .type("quiz")
            .correctOptionId(correctOption)
            .isAnonymous(false)
            .build()

        val message = telegramClient.execute(poll)

        return message.poll.id
    }

    fun editMessage(
        chatId: String,
        messageId: Int,
        text: String,
        keyboard: InlineKeyboardMarkup
    ) {

        val edit = EditMessageText.builder()
            .chatId(chatId)
            .messageId(messageId)
            .text(text)
            .replyMarkup(keyboard)
            .build()

        telegramClient.execute(edit)
    }

    fun answerCallback(callbackId: String) {
        telegramClient.execute(
            AnswerCallbackQuery.builder()
                .callbackQueryId(callbackId)
                .build()
        )
    }
}