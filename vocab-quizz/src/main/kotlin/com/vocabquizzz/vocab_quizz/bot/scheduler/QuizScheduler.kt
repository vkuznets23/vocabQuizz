package com.vocabquizzz.vocab_quizz.bot.scheduler

import com.vocabquizzz.vocab_quizz.bot.handler.QuizHandler
import com.vocabquizzz.vocab_quizz.service.UserQuizSettingsService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class QuizScheduler(
    private val settingsService: UserQuizSettingsService,
    private val quizHandler: QuizHandler,
) {

    @Scheduled(cron = "\${quiz.cron}")
    fun sendQuiz() {
        settingsService.findEnabled()
            .filter {settingsService.isEligibleNow(it)}
            .forEach {settings -> 
                val chatId = settings.user.telegramUserId.toString()
                if (quizHandler.startLesson(chatId)) {
                    settingsService.markQuizSent(settings)
                }
            }
    }
}