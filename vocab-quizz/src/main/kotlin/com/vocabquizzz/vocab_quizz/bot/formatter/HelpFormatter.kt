package com.vocabquizzz.vocab_quizz.bot.formatter

import org.springframework.stereotype.Component
import com.vocabquizzz.vocab_quizz.model.word.WordConstraints

@Component
class HelpFormatter {
    fun formatHelp(questionsPerLesson: Int) = """
        🤖 Vocab Quiz Bot

        📚 Vocabulary

        📝 /add <word>
        Add a word. I'll ask for definition, then example (or /skip).

        ✏️ /edit <word>
        Edit word, definition, or example. Choose: 1/word, 2/definition, 3/example.
        For example use /skip to clear it.

        🗑️ /delete <word>
        Delete a word from your vocabulary.

        📖 /word <word>
        Show definition and example. Suggests similar words if not found.

        📚 /words
        List your words (paginated). Use ⬅️ ➡️ buttons to navigate.

        🧠 Quiz

        /quiz
        Start a quiz: $questionsPerLesson questions, different question types.
        Set the number of questions in /settings.
        You need at least ${WordConstraints.MIN_WORDS_FOR_QUIZ} words in your dictionary.
        Answer using the poll buttons Telegram sends you.

        📊 Statistics

        /stat
        Your overall quiz stats (words practiced, accuracy, etc.).

        /stat <word>
        Stats for a specific word.

        ⚙️ Other

        ❌ /cancel
        Cancel current add / edit / quiz session.

        ❓ /help
        Show this message.

        /settings
        Configure auto-quiz schedule, interval, and questions per quiz.

        💡 Tips
        • Example sentences: replace the word with ___
        e.g. I ate an ___ for breakfast.
        • Word max ${WordConstraints.MAX_WORD_LENGTH} chars, definition/example max ${WordConstraints.MAX_DEFINITION_LENGTH} chars.
        """.trimIndent()
}