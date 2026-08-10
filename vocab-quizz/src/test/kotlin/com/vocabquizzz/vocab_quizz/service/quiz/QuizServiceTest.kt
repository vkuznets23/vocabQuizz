package com.vocabquizzz.vocab_quizz.service.quiz

import com.vocabquizzz.vocab_quizz.entity.User
import com.vocabquizzz.vocab_quizz.entity.Word
import com.vocabquizzz.vocab_quizz.model.QuizType
import com.vocabquizzz.vocab_quizz.service.quiz.QuizService
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuizServiceTest {
    
    private val quizService = QuizService()
    private val user = User(id = 1, telegramUserId = 123)
    private fun word(
        id: Long,
        text: String,
        definition: String = "def",
        example: String? = null,
    ) = Word(id = id, user = user, word = text, definition = definition, example = example)

    @Test
    fun `generateQuiz returns 4 options including correct answer`() {
        val target = word(1, "apple", "a fruit", "I like ____")
        val others = listOf(
            word(2, "banana", "yellow fruit", "Monkey eats ____"),
            word(3, "car", "vehicle", "I drive ____"),
            word(4, "dog", "animal", "Dog ____ ____"),
        )

        val quiz = quizService.generateQuiz(target, others)

        assertTrue(quiz.options.contains(quiz.correct))
        assertEquals(1L, quiz.wordId)

        when (quiz.type) {
            QuizType.TRUE_FALSE -> assertEquals(2, quiz.options.size)
            QuizType.WORD_TO_DEFINITION,
            QuizType.DEFINITION_TO_WORD,
            QuizType.SENTENCE -> assertEquals(4, quiz.options.size)
        }
    }
}