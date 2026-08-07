package com.vocabquizzz.vocab_quizz.bot.handler

import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import com.vocabquizzz.vocab_quizz.entity.Word
import com.vocabquizzz.vocab_quizz.entity.User
import com.vocabquizzz.vocab_quizz.service.WordService
import com.vocabquizzz.vocab_quizz.service.UserService
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import org.springframework.data.domain.Page



@Service
class AllWordHandler(
    private val wordService: WordService,
    private val userService: UserService,
    private val telegramGateway: TelegramGateway
) {

    fun handle(message: Message) {
        val chatId = message.chatId.toString()
        val user = userService.getOrCreate(chatId.toLong())

        val (text, keyboard) = buildPage(user, 0)

        telegramGateway.sendTextWithKeyboard(
            chatId,
            text,
            keyboard
        )
    }

    fun buildPage(
        user: User,
        pageNumber: Int
    ): Pair<String, InlineKeyboardMarkup> {

        val page = wordService.getWords(user, pageNumber)

        if (page.isEmpty) {
            return Pair(
                "You don't have any words yet.",
                InlineKeyboardMarkup.builder().build()
            )
        }

        val text = buildString {
            appendLine("📚 Your vocabulary:")
            appendLine()

            page.content.forEachIndexed { index, word ->
                val number = page.number * page.size + index + 1
                appendLine("$number. ${word.word}")
            }

            appendLine()
            append("Page ${page.number + 1} of ${page.totalPages}")
        }

        return Pair(text, buildKeyboard(page))
    }

    private fun buildKeyboard(page: Page<Word>): InlineKeyboardMarkup {
        val buttons = mutableListOf<InlineKeyboardButton>()

        if (!page.isFirst) {
            buttons += InlineKeyboardButton.builder()
                .text("⬅️")
                .callbackData("words:${page.number - 1}")
                .build()
        }

        if (!page.isLast) {
            buttons += InlineKeyboardButton.builder()
                .text("➡️")
                .callbackData("words:${page.number + 1}")
                .build()
        }

        return InlineKeyboardMarkup.builder()
            .keyboardRow(InlineKeyboardRow(buttons))
            .build()
    }
}