package com.vocabquizzz.vocab_quizz.gateway

import com.vocabquizzz.vocab_quizz.model.Quiz
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.generics.TelegramClient
import org.telegram.telegrambots.meta.api.objects.polls.input.InputPollOption

@Component
class TelegramGateway(
    private val telegramClient: TelegramClient
) {

    fun sendText(chatId: String, text: String) {
        telegramClient.execute(
            SendMessage(chatId, text)
        )
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
}