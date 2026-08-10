package com.vocabquizzz.vocab_quizz.bot.formatter

import org.springframework.stereotype.Component
import com.vocabquizzz.vocab_quizz.model.stats.UserStatistic
import com.vocabquizzz.vocab_quizz.model.stats.WordStatistic

@Component
class StatisticFormatter {

    fun formatUserStatistic(stat: UserStatistic) = """
        📊 Your statistics
        Words in dictionary: ${stat.totalWords}
        Words practiced: ${stat.wordsAttempted}
        Total attempts: ${stat.totalAttempts}
        Correct: ${stat.correctAnswers}
        Wrong: ${stat.wrongAnswers}
        Accuracy: ${"%.2f".format(stat.accuracy)}%
    """.trimIndent()

    fun formatWordStatistic(stat: WordStatistic) = """
        📊 Statistics for "${stat.word}"
        Attempts: ${stat.totalAttempts}
        Correct: ${stat.correctAnswers}
        Wrong: ${stat.wrongAnswers}
        Accuracy: ${"%.2f".format(stat.accuracy)}%
    """.trimIndent()

}