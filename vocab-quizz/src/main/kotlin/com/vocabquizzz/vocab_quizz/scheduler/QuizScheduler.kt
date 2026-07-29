package com.vocabquizzz.vocab_quizz.scheduler

import com.vocabquizzz.vocab_quizz.bot.TelegramBot
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class QuizScheduler(
    private val telegramBot: TelegramBot
) {

    @Scheduled(cron = "\${quiz.cron}")
    fun sendQuiz() {
        telegramBot.sendQuiz("410458864")
    }
}