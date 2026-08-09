package com.vocabquizzz.vocab_quizz.service

import org.springframework.stereotype.Service
import com.vocabquizzz.vocab_quizz.service.AddWordSessionService
import com.vocabquizzz.vocab_quizz.service.EditWordSessionService
import com.vocabquizzz.vocab_quizz.service.LessonSessionService
import com.vocabquizzz.vocab_quizz.model.CancelResult

@Service
class SessionCancelService(
    private val addWordSessionService: AddWordSessionService,
    private val editWordSessionService: EditWordSessionService,
    private val lessonSessionService: LessonSessionService,
) {

    fun cancelAll(chatId: String): CancelResult {

        when {
            addWordSessionService.isRunning(chatId) -> { 
                addWordSessionService.finish(chatId)
                return CancelResult.Cancelled("❌ Add word cancelled.")
            }
            editWordSessionService.isRunning(chatId) -> { 
                editWordSessionService.finish(chatId)
                return CancelResult.Cancelled("❌ Edit cancelled.")
            }
            lessonSessionService.cancelLesson(chatId) -> return CancelResult.Cancelled("❌ Quiz cancelled.")
            else -> return CancelResult.NothingToCancel()

        }

    }
}