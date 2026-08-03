package com.vocabquizzz.vocab_quizz.scheduler

import com.vocabquizzz.vocab_quizz.service.BotService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class QuizScheduler(
    private val botService: BotService
) {

    @Scheduled(cron = "\${quiz.cron}")
    fun sendQuiz() {
        botService.startLesson("410458864")
    }
}