package com.vocabquizzz.vocab_quizz.bot

import org.telegram.telegrambots.meta.api.methods.polls.SendPoll
import com.vocabquizzz.vocab_quizz.repository.WordRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
@Component
class TelegramBot(
    @Value("\${telegram.bot.token}")
    private val token: String,

    @Value("\${telegram.bot.username}")
    private val username: String,

    private val wordRepository: WordRepository
) : TelegramLongPollingBot(token) {

    override fun getBotUsername(): String {
        return username
    }

    fun sendQuiz(chatId: String) {
        try {
            val quiz = wordRepository.getQuiz()

            val poll = SendPoll.builder()
                .chatId(chatId)
                .question("What does '${quiz.word}' mean?")
                .options(quiz.options)
                .type("quiz")
                .correctOptionId(quiz.options.indexOf(quiz.correct))
                .isAnonymous(false)
                .build()

            execute(poll)
        } catch (e: Exception) {
            println("Failed to send quiz to $chatId: ${e.message}")
        }
    }

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage() && update.message.hasText()) {
            val chatId = update.message.chatId.toString()

            if (update.message.text == "/quiz") {
                println("ChatId = ${update.message.chatId}")
                sendQuiz(chatId)
            
            } else {
                execute(
                    SendMessage(
                        chatId,
                        "Vocab Quizz Bot 📚\nTry /quiz"
                    )
                )
            }
        }
    }
}