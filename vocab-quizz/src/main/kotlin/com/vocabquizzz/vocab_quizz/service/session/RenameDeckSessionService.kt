package com.vocabquizzz.vocab_quizz.service.session

import org.springframework.stereotype.Service
import com.vocabquizzz.vocab_quizz.model.session.RenameDeckSession


@Service
class RenameDeckSessionService {

    private val sessions = mutableMapOf<String, RenameDeckSession>()

    fun start(chatId: String): Boolean {
        if (sessions.containsKey(chatId)) {
            return false
        }

        sessions[chatId] = RenameDeckSession()

        return true
    }

    fun get(chatId: String): RenameDeckSession? {
        return sessions[chatId]
    }

    fun finish(chatId: String): RenameDeckSession? {
        return sessions.remove(chatId)
    }

    fun isRunning(chatId: String): Boolean {
        return sessions.containsKey(chatId)
    }
}