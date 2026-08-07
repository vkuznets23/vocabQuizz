package com.vocabquizzz.vocab_quizz.bot.handler

import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import com.vocabquizzz.vocab_quizz.service.UserService
import com.vocabquizzz.vocab_quizz.service.QuizService
import com.vocabquizzz.vocab_quizz.service.WordService
import com.vocabquizzz.vocab_quizz.service.LessonSessionService
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer

@Service
class QuizHandler(
    private val lessonService: LessonSessionService,
    private val quizService: QuizService,
    private val wordService: WordService,
    private val userService: UserService,
    private val telegramGateway: TelegramGateway
) {

    fun handleQuiz(message: Message) {

        val chatId = message.chatId.toString()
        val user = userService.getOrCreate(chatId.toLong())

        if (wordService.countWords(user) < 4) {
            telegramGateway.sendText(
                chatId,
                "You need at least 4 words before starting a quiz."
            )
            return
        }

        startLesson(chatId)
    }

    fun startLesson(chatId: String){
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

    private fun finishLesson(chatId: String) {

        val session = lessonService.finishLesson(chatId)

        telegramGateway.sendText(
            chatId,
            "🎉 Quiz finished!\nScore: ${session?.score}/5"
        )
    }


    fun handlePoll(answer: PollAnswer) {

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

}