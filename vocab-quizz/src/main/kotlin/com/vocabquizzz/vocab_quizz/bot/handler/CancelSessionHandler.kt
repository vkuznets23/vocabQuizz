package com.vocabquizzz.vocab_quizz.bot.handler

import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import com.vocabquizzz.vocab_quizz.service.AddWordSessionService
import com.vocabquizzz.vocab_quizz.service.EditWordSessionService
import com.vocabquizzz.vocab_quizz.service.LessonSessionService
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.message.Message

@Service
class CancelSessionHandler(
    private val addWordSessionService: AddWordSessionService,
    private val editWordSessionService: EditWordSessionService,
    private val lessonSessionService: LessonSessionService,
    private val telegramGateway: TelegramGateway
) {

    fun handle(message: Message) {
        val chatId = message.chatId.toString()

        var cancelled = false

        if (addWordSessionService.isRunning(chatId)) {
            addWordSessionService.finish(chatId)
            cancelled = true
        }

        if (editWordSessionService.isRunning(chatId)) {
            editWordSessionService.finish(chatId)
            cancelled = true
        }

        if (lessonSessionService.isRunning(chatId)) {
            lessonSessionService.finishLesson(chatId)
            cancelled = true
        }

        telegramGateway.sendText(
            chatId,
            if (cancelled)
                "❌ Current operation cancelled."
            else
                "Nothing to cancel."
        )
    }

}