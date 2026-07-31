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
    data class QuizSession(
        var remaining: Int,
        var score: Int
    )

    private val sessions = mutableMapOf<String, QuizSession>()
    private val activePolls = mutableMapOf<String, String>()

    override fun getBotUsername(): String {
        return username
    }

    fun sendQuiz(chatId: String) {
        try {
            val quiz = wordRepository.getQuiz()

            val poll = SendPoll.builder()
                .chatId(chatId)
                .question(quiz.question)
                .options(quiz.options)
                .type("quiz")
                .correctOptionId(quiz.options.indexOf(quiz.correct))
                .isAnonymous(false)
                .build()

            val message = execute(poll)
            activePolls[message.poll.id] = chatId
        } catch (e: Exception) {
            println("Failed to send quiz to $chatId: ${e.message}")
        }
    }

    fun startLesson(chatId: String) {
        sessions[chatId] = QuizSession(
            remaining = 5,
            score = 0
        )

        sendQuiz(chatId)
    }

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage() && update.message.hasText()) {
            val chatId = update.message.chatId.toString()

            if (update.message.text == "/quiz") {
                if (sessions.containsKey(chatId)) {
                    execute(
                        SendMessage(
                            chatId,
                            "You're already taking a quiz 😊"
                        )
                    )
                    return
                }

                startLesson(chatId)
            } else {
                execute(
                    SendMessage(
                        chatId,
                        "Vocab Quizz Bot 📚\nTry /quiz"
                    )
                )
            }
        }
        if (update.hasPollAnswer()) {
            val pollId = update.pollAnswer.pollId

            val chatId = activePolls.remove(pollId)

            if (chatId != null) {
            val session = sessions[chatId]

            if (session != null) {
                session.remaining--

                if (session.remaining > 0) {
                    sendQuiz(chatId)
                } else {
                    execute(
                        SendMessage(
                            chatId,
                            "🎉 Quiz finished!"
                        )
                    )

                    sessions.remove(chatId)
                }
            }
        }
        }
    }
}