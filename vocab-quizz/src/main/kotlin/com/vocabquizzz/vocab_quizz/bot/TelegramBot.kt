package com.vocabquizzz.vocab_quizz.bot

import com.vocabquizzz.vocab_quizz.service.LessonService
import com.vocabquizzz.vocab_quizz.service.QuizService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class TelegramBot(
    @Value("\${telegram.bot.token}")
    private val token: String,

    @Value("\${telegram.bot.username}")
    private val username: String,

    private val quizService: QuizService,
    private val lessonService: LessonService
) : TelegramLongPollingBot(token) {

    override fun getBotUsername() = username

    override fun onUpdateReceived(update: Update) {

        if (update.hasMessage() && update.message.hasText()) {

            val chatId = update.message.chatId.toString()

            when (update.message.text) {

                "/quiz" -> startLesson(chatId)

                else -> execute(
                    SendMessage(
                        chatId,
                        "📚 Vocab Quiz Bot\nTry /quiz"
                    )
                )
            }
        }

        if (update.hasPollAnswer()) {

            val pollAnswer = update.pollAnswer

            val activePoll = lessonService.finishPoll(pollAnswer.pollId)
                ?: return

            val chosenOption = pollAnswer.optionIds.first()

            val hasNext = lessonService.answer(
                chatId = activePoll.chatId,
                correct = chosenOption == activePoll.correctOption
            )

            if (hasNext) {
                sendQuiz(activePoll.chatId)
            } else {

                val session = lessonService.finishLesson(activePoll.chatId)

                execute(
                    SendMessage(
                        activePoll.chatId,
                        "🎉 Quiz finished!\n" +
                                "Score: ${session?.score}/5"
                    )
                )
            }
        }
    }

    fun startLesson(chatId: String) {

        if (!lessonService.startLesson(chatId)) {

            execute(
                SendMessage(
                    chatId,
                    "You're already taking a quiz 😊"
                )
            )

            return
        }

        sendQuiz(chatId)
    }

    private fun sendQuiz(chatId: String) {

        val quiz = quizService.getQuiz()

        val correctOption = quiz.options.indexOf(quiz.correct)

        val poll = SendPoll.builder()
            .chatId(chatId)
            .question(quiz.question)
            .options(quiz.options)
            .type("quiz")
            .correctOptionId(correctOption)
            .isAnonymous(false)
            .build()

        val message = execute(poll)

        lessonService.registerPoll(
            pollId = message.poll.id,
            chatId = chatId,
            correctOption = correctOption
        )
    }
}