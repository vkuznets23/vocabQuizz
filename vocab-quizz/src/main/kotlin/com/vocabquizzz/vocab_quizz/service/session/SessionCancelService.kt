package com.vocabquizzz.vocab_quizz.service.session

import org.springframework.stereotype.Service
import com.vocabquizzz.vocab_quizz.service.session.AddWordSessionService
import com.vocabquizzz.vocab_quizz.service.session.EditWordSessionService
import com.vocabquizzz.vocab_quizz.service.quiz.LessonSessionService
import com.vocabquizzz.vocab_quizz.model.session.CancelResult

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