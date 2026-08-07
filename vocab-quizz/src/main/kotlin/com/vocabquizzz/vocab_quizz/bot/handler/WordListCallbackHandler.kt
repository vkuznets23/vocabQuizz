package com.vocabquizzz.vocab_quizz.bot.handler

import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import org.springframework.stereotype.Service
import com.vocabquizzz.vocab_quizz.bot.handler.AllWordHandler
import com.vocabquizzz.vocab_quizz.service.UserService
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

@Service
class WordListCallbackHandler(
    private val allWordHandler: AllWordHandler,
    private val userService: UserService,
    private val telegramGateway: TelegramGateway,
) {
    fun handle(callback: CallbackQuery) {

        val page =
            callback.data.removePrefix("words:").toInt()

        val chatId =
            callback.message.chatId.toString()

        val messageId =
            callback.message.messageId

        val user =
            userService.getOrCreate(chatId.toLong())

        val (text, keyboard) =
            allWordHandler.buildPage(user, page)

        telegramGateway.editMessage(
            chatId,
            messageId,
            text,
            keyboard
        )

        telegramGateway.answerCallback(callback.id)
    }
}