package com.vocabquizzz.vocab_quizz.bot.handler

import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import com.vocabquizzz.vocab_quizz.service.word.WordService
import com.vocabquizzz.vocab_quizz.service.user.UserService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.message.Message
import com.vocabquizzz.vocab_quizz.bot.formatter.AllWordsListFormatter

@Component
class AllWordsHandler(
    private val wordService: WordService,
    private val userService: UserService,
    private val allWordsListFormatter: AllWordsListFormatter,
    private val telegramGateway: TelegramGateway
) {

    fun handle(message: Message) {
        val chatId = message.chatId.toString()
        val user = userService.getOrCreate(chatId.toLong())

        val (text, keyboard) = allWordsListFormatter.formatPage(wordService.getWords(user, 0))

        telegramGateway.sendTextWithKeyboard(
            chatId,
            text,
            keyboard
        )
    }
}