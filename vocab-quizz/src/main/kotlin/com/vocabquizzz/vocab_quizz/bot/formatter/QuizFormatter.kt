package com.vocabquizzz.vocab_quizz.bot.formatter

import org.springframework.stereotype.Component
import com.vocabquizzz.vocab_quizz.model.word.WordConstraints

@Component
class QuizFormatter {
    fun notEnoughWords() =
        "You need at least ${WordConstraints.MIN_WORDS_FOR_QUIZ} words before starting a quiz."

    fun alreadyRunning() = "You're already taking a quiz 😊"

    fun finished(score: Int, total: Int) =
        "🎉 Quiz finished!\nScore: $score/$total"
}