package com.vocabquizzz.vocab_quizz.service

import com.vocabquizzz.vocab_quizz.model.QuizSession
import org.springframework.stereotype.Service

@Service
class LessonService {
    data class ActivePoll(
        val chatId: String,
        val correctOption: Int
    )

    private val sessions = mutableMapOf<String, QuizSession>()
    private val activePolls = mutableMapOf<String, ActivePoll>()

    fun startLesson(chatId: String, questions: Int = 5): Boolean {
        if (sessions.containsKey(chatId)) {
            return false
        }

        sessions[chatId] = QuizSession(
            remaining = questions,
            score = 0
        )

        return true
    }

    fun registerPoll(
        pollId: String,
        chatId: String,
        correctOption: Int
    ) {
        activePolls[pollId] = ActivePoll(chatId, correctOption)
    }

    fun finishPoll(pollId: String): ActivePoll? {
        return activePolls.remove(pollId)
    }

    fun answer(chatId: String, correct: Boolean): Boolean {
        val session = sessions[chatId] ?: return false

        if (correct) {
            session.score++
        }

        session.remaining--

        return session.remaining > 0
    }

    fun finishLesson(chatId: String): QuizSession? {
        return sessions.remove(chatId)
    }

    fun isLessonRunning(chatId: String): Boolean {
        return sessions.containsKey(chatId)
    }
}