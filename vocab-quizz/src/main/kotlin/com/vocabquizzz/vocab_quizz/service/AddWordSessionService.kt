package com.vocabquizzz.vocab_quizz.service

import org.springframework.stereotype.Service
import com.vocabquizzz.vocab_quizz.model.AddWordSession


@Service
class AddWordSessionService {

    private val sessions = mutableMapOf<String, AddWordSession>()

    fun start(chatId: String): Boolean {
        if (sessions.containsKey(chatId)) {
            return false
        }

        sessions[chatId] = AddWordSession()

        return true
    }

    fun get(chatId: String): AddWordSession? {
        return sessions[chatId]
    }

    fun finish(chatId: String): AddWordSession? {
        return sessions.remove(chatId)
    }

    fun isRunning(chatId: String): Boolean {
        return sessions.containsKey(chatId)
    }
}