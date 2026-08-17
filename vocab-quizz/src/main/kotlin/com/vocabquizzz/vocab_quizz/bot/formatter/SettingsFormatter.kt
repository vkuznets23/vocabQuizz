package com.vocabquizzz.vocab_quizz.bot.formatter

import com.vocabquizzz.vocab_quizz.entity.UserQuizSettings
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.*
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import java.time.format.DateTimeFormatter

@Component
class SettingsFormatter {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun formatMain(settings: UserQuizSettings): Pair<String, InlineKeyboardMarkup> {
        val text = buildString {
            appendLine("⚙️ Quiz settings")
            appendLine()
            appendLine("Auto quizzes: ${if (settings.enabled) "ON ✅" else "OFF ❌"}")
            appendLine("Every: ${settings.intervalHours} hour(s)")
            appendLine(
                "Active hours: ${settings.windowStart.format(timeFormatter)} – " +
                    "${settings.windowEnd.format(timeFormatter)} (${settings.timezone})"
            )
            appendLine("Questions per quiz: ${settings.questionsPerLesson}")
        }

        return text to buildKeyboard(settings)
    }

    private fun buildKeyboard(settings: UserQuizSettings): InlineKeyboardMarkup {
        val rows = mutableListOf<InlineKeyboardRow>()

        // Toggle
        rows += InlineKeyboardRow(
            listOf(
                button(
                    text = if (settings.enabled) "Turn OFF" else "Turn ON",
                    callbackData = "settings:toggle",
                )
            )
        )

        // Interval
        rows += InlineKeyboardRow(
            listOf(1, 2, 3). map { hours -> 
                button (
                    text = selectedLabel("${hours}h", settings.intervalHours == hours),
                    callbackData = "settings:interval:$hours",
                )
            }
        )

        // Questions per lesson
        rows += InlineKeyboardRow(
            listOf(5, 7, 10). map { n -> 
                button (
                    text = selectedLabel("${n} questions", settings.questionsPerLesson == n),
                    callbackData = "settings:questionsPerLesson:$n",
                )
            }
        )

        // Active hours presets
        rows += InlineKeyboardRow(
            listOf(
                windowButton("9–18", "9-18", settings),
                windowButton("8–20", "8-20", settings),
                windowButton("10–22", "10-22", settings),
            )
        )

        return InlineKeyboardMarkup.builder()
            .keyboard(rows)
            .build()
    }

    private fun windowButton(
        label: String,
        preset: String,
        settings: UserQuizSettings,
    ): InlineKeyboardButton {
        val selected = isWindowPresetSelected(settings, preset)
        return button(
            text = selectedLabel(label, selected),
            callbackData = "settings:window:$preset",
        )
    }

    private fun isWindowPresetSelected(settings: UserQuizSettings, preset: String): Boolean {
        val parts = preset.split("-")
        if (parts.size != 2) return false
        val start = parts[0].toIntOrNull() ?: return false
        val end = parts[1].toIntOrNull() ?: return false
        return settings.windowStart.hour == start && settings.windowEnd.hour == end
    }

    private fun selectedLabel(label: String, selected: Boolean): String =
        if (selected) "✓ $label" else label

    private fun button(text: String, callbackData: String): InlineKeyboardButton =
        InlineKeyboardButton.builder()
            .text(text)
            .callbackData(callbackData)
            .build()

}