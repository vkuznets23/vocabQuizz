package com.vocabquizzz.vocab_quizz.bot.formatter

import com.vocabquizzz.vocab_quizz.entity.Word
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow


@Component
class AllWordsListFormatter {
    fun formatPage(
        page: Page<Word>
    ): Pair<String, InlineKeyboardMarkup> {
        if (page.isEmpty) {
            return "You don't have any words yet." to emptyKeyboard()
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

    private fun emptyKeyboard(): InlineKeyboardMarkup =
        InlineKeyboardMarkup.builder().build()

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