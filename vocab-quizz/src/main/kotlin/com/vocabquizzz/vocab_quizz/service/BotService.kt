package com.vocabquizzz.vocab_quizz.service

import com.vocabquizzz.vocab_quizz.gateway.TelegramGateway
import com.vocabquizzz.vocab_quizz.service.UserService
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer

@Service
class BotService(
    private val lessonService: LessonService,
    private val quizService: QuizService,
    private val userService: UserService,
    private val telegramGateway: TelegramGateway
) {

    fun handleMessage(message: Message) {

        val chatId = message.chatId.toString()

        when (message.text) {

            "/quiz" -> startLesson(chatId)

            else -> telegramGateway.sendText(
                chatId,
                "📚 Vocab Quiz Bot\nTry /quiz"
            )
        }
    }

    fun startLesson(chatId: String) {

        if (!lessonService.startLesson(chatId)) {

            telegramGateway.sendText(
                chatId,
                "You're already taking a quiz 😊"
            )

            return
        }

        sendQuiz(chatId)
    }

    private fun sendQuiz(chatId: String) {

        val user = userService.getOrCreate(chatId.toLong())
        val quiz = quizService.getQuiz(user)

        val pollId = telegramGateway.sendQuiz(
            chatId,
            quiz
        )

        lessonService.registerPoll(
            pollId = pollId,
            chatId = chatId,
            correctOption = quiz.options.indexOf(quiz.correct)
        )
    }

    fun handlePollAnswer(answer: PollAnswer) {

        val activePoll =
            lessonService.finishPoll(answer.pollId)
                ?: return

        val selectedOption = answer.optionIds.first()

        val hasNext = lessonService.answer(
            chatId = activePoll.chatId,
            correct = selectedOption == activePoll.correctOption
        )

        if (hasNext) {
            sendQuiz(activePoll.chatId)
        } else {
            finishLesson(activePoll.chatId)
        }
    }

    private fun finishLesson(chatId: String) {

        val session = lessonService.finishLesson(chatId)

        telegramGateway.sendText(
            chatId,
            "🎉 Quiz finished!\nScore: ${session?.score}/5"
        )
    }
}