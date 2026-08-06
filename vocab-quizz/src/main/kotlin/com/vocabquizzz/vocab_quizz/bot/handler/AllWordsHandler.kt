package com.vocabquizzz.vocab_quizz.bot.handler

import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import com.vocabquizzz.vocab_quizz.entity.Word
import com.vocabquizzz.vocab_quizz.service.WordService
import com.vocabquizzz.vocab_quizz.service.UserService
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.message.Message

@Service
class AllWordHandler(
    private val wordService: WordService,
    private val userService: UserService,
    private val telegramGateway: TelegramGateway

) {
    fun handle(message: Message) {
        val chatId = message.chatId.toString()
        val user = userService.getOrCreate(chatId.toLong())
        
        val words: List<Word> = wordService.getAllWords(user)
        if (words.isEmpty()) {
            telegramGateway.sendText(chatId, "You don't have any words yet.")
            return
        }
        
        val text = buildString {
            appendLine("📚 Your vocabulary:")
            appendLine()

            words.forEachIndexed { index, word ->
                appendLine("${index + 1}. ${word.word}")
            }
        }

        telegramGateway.sendText(
            chatId,
            text
        )

    }
}