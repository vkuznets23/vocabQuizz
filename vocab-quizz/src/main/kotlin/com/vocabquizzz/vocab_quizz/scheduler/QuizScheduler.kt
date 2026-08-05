package com.vocabquizzz.vocab_quizz.scheduler

import com.vocabquizzz.vocab_quizz.bot.handler.QuizHandler
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class QuizScheduler(
    private val quizHandler: QuizHandler
) {

    @Scheduled(cron = "\${quiz.cron}")
    fun sendQuiz() {
        quizHandler.startLesson("410458864")
    }
}