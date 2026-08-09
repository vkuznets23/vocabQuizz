package com.vocabquizzz.vocab_quizz.bot.handler

import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import org.springframework.stereotype.Component
import com.vocabquizzz.vocab_quizz.service.UserService
import com.vocabquizzz.vocab_quizz.service.WordService
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import com.vocabquizzz.vocab_quizz.bot.formatter.AllWordsListFormatter

@Component
class AllWordsCallbackHandler(
    private val userService: UserService,
    private val allWordsListFormatter: AllWordsListFormatter,
    private val telegramGateway: TelegramGateway,
    private val wordService: WordService,
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
            allWordsListFormatter.formatPage(wordService.getWords(user, page))

        telegramGateway.editMessage(
            chatId,
            messageId,
            text,
            keyboard
        )

        telegramGateway.answerCallback(callback.id)
    }
}