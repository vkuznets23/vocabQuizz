package com.vocabquizzz.vocab_quizz.service

import org.springframework.stereotype.Service
import com.vocabquizzz.vocab_quizz.model.EditWordSession


@Service
class EditWordSessionService {

    private val sessions = mutableMapOf<String, EditWordSession>()

    fun start(chatId: String): Boolean {
        if (sessions.containsKey(chatId)) {
            return false
        }

        sessions[chatId] = EditWordSession()

        return true
    }

    fun get(chatId: String): EditWordSession? {
        return sessions[chatId]
    }

    fun finish(chatId: String): EditWordSession? {
        return sessions.remove(chatId)
    }

    fun isRunning(chatId: String): Boolean {
        return sessions.containsKey(chatId)
    }
}