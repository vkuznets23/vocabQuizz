package com.vocabquizzz.vocab_quizz.bot.handler

import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import com.vocabquizzz.vocab_quizz.service.user.UserService
import com.vocabquizzz.vocab_quizz.service.quiz.LessonSessionService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer
import com.vocabquizzz.vocab_quizz.service.quiz.QuizFlowService
import com.vocabquizzz.vocab_quizz.bot.formatter.QuizFormatter
import com.vocabquizzz.vocab_quizz.model.quiz.StartQuizResult
import com.vocabquizzz.vocab_quizz.model.quiz.AnswerResult
import com.vocabquizzz.vocab_quizz.model.quiz.Quiz

@Component
class QuizHandler(
    private val quizFlowService: QuizFlowService,
    private val lessonSessionService: LessonSessionService,
    private val userService: UserService,
    private val telegramGateway: TelegramGateway,
    private val quizFormatter: QuizFormatter,
) {

    fun handleQuiz(message: Message) {

        val chatId = message.chatId.toString()
        val user = userService.getOrCreate(chatId.toLong())

        when (val result = quizFlowService.startQuiz(chatId, user)) {
            is StartQuizResult.Ready -> sendQuiz(result.chatId, result.quiz)
            StartQuizResult.NotEnoughWords ->
                telegramGateway.sendText(chatId, quizFormatter.notEnoughWords())
            StartQuizResult.AlreadyRunning ->
                telegramGateway.sendText(chatId, quizFormatter.alreadyRunning())
        }
    }

    fun startLesson(chatId: String): Boolean {  // for QuizScheduler
        val user = userService.getOrCreate(chatId.toLong())
        when (val result = quizFlowService.startQuiz(chatId, user)) {
            is StartQuizResult.Ready -> sendQuiz(result.chatId, result.quiz)
            else -> return false
        }
        return true
    }


    fun handlePoll(answer: PollAnswer) {
        val selectedOption = answer.optionIds.first()
        when (val result = quizFlowService.processAnswer(answer.pollId, selectedOption)) {
            is AnswerResult.NextQuestion -> sendQuiz(result.chatId, result.quiz)
            is AnswerResult.Finished ->
                telegramGateway.sendText(
                    result.chatId,
                    quizFormatter.finished(result.score, result.total),
                )
            AnswerResult.UnknownPoll -> return
        }
    }

    private fun sendQuiz(chatId: String, quiz: Quiz) {
        val pollId = telegramGateway.sendQuiz(chatId, quiz)
        lessonSessionService.registerPoll(
            pollId = pollId,
            chatId = chatId,
            wordId = quiz.wordId,
            quizType = quiz.type,
            correctOption = quiz.options.indexOf(quiz.correct),
        )
    }

}