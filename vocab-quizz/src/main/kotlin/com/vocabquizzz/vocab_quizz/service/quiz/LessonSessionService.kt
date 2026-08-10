package com.vocabquizzz.vocab_quizz.service.quiz

import org.springframework.stereotype.Service
import com.vocabquizzz.vocab_quizz.model.quiz.QuizSession
import com.vocabquizzz.vocab_quizz.model.quiz.QuizType
import com.vocabquizzz.vocab_quizz.model.word.WordConstraints
import com.vocabquizzz.vocab_quizz.model.quiz.ActivePoll

@Service
class LessonSessionService {

    private val sessions = mutableMapOf<String, QuizSession>()
    private val activePolls = mutableMapOf<String, ActivePoll>()

    fun startLesson(chatId: String, questions: Int = WordConstraints.QUESTIONS_PER_LESSON,
    ): Boolean {
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
        wordId: Long,
        quizType: QuizType,
        correctOption: Int

    ) {
        activePolls[pollId] = ActivePoll(
            chatId = chatId,
            wordId = wordId,
            quizType = quizType,
            correctOption = correctOption
        )
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

    fun isRunning(chatId: String): Boolean {
        return sessions.containsKey(chatId)
    }

    fun finishLesson(chatId: String): QuizSession? {
        return sessions.remove(chatId)
    }

    fun cancelLesson(chatId: String): Boolean {
        if (!sessions.containsKey(chatId)) return false

        sessions.remove(chatId)
        activePolls.entries.removeIf { it.value.chatId == chatId }
        return true
    }
}