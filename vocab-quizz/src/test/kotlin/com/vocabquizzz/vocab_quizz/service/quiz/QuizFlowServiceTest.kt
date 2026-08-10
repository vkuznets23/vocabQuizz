package com.vocabquizzz.vocab_quizz.service.quiz

import com.vocabquizzz.vocab_quizz.entity.User
import com.vocabquizzz.vocab_quizz.entity.Word
import com.vocabquizzz.vocab_quizz.model.quiz.*
import com.vocabquizzz.vocab_quizz.model.word.WordConstraints
import com.vocabquizzz.vocab_quizz.service.word.WordAttemptService
import com.vocabquizzz.vocab_quizz.service.word.WordService
import com.vocabquizzz.vocab_quizz.service.user.UserService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuizFlowServiceTest {

    private val wordService: WordService = mock()
    private val userService: UserService = mock()
    private val wordAttemptService: WordAttemptService = mock()

    private val quizService = QuizService()
    private val lessonSessionService = LessonSessionService()
    private val quizDeckService = QuizDeckService(wordService)

    private val quizFlowService = QuizFlowService(
        wordService = wordService,
        quizService = quizService,
        userService = userService,
        quizDeckService = quizDeckService,
        lessonSessionService = lessonSessionService,
        wordAttemptService = wordAttemptService,
    )

    private val user = User(id = 1, telegramUserId = 123)
    private val chatId = "123"

    private fun word(id: Long, text: String) =
        Word(id = id, user = user, word = text, definition = "def")

    private val words = listOf(
        word(1, "apple"),
        word(2, "banana"),
        word(3, "car"),
        word(4, "dog"),
    )

    private fun stubWords() {
        whenever(wordService.hasEnoughWordsForQuiz(user)).thenReturn(true)
        whenever(wordService.getAllWords(user)).thenReturn(words)
    }

    @Test
    fun `startQuiz returns NotEnoughWords when below minimum`() {
        whenever(wordService.hasEnoughWordsForQuiz(user)).thenReturn(false)

        val result = quizFlowService.startQuiz(chatId, user)

        assertTrue(result is StartQuizResult.NotEnoughWords)
    }

    @Test
    fun `startQuiz returns AlreadyRunning when lesson active`() {
        stubWords()
        quizFlowService.startQuiz(chatId, user)

        val result = quizFlowService.startQuiz(chatId, user)

        assertTrue(result is StartQuizResult.AlreadyRunning)
    }

    @Test
    fun `startQuiz returns Ready with quiz`() {
        stubWords()

        val result = quizFlowService.startQuiz(chatId, user)

        assertTrue(result is StartQuizResult.Ready)
        assertEquals(chatId, result.chatId)
        assertTrue(result.quiz.options.contains(result.quiz.correct))
    }

    private fun registerPoll(
        pollId: String = "poll-1",
        wordId: Long = 1L,
        correctOption: Int = 0,
        questions: Int = 5, // how many questions in the lesson
    ) {
        lessonSessionService.startLesson(chatId, questions = questions)
        lessonSessionService.registerPoll(
            pollId = pollId,
            chatId = chatId,
            wordId = wordId,
            quizType = QuizType.WORD_TO_DEFINITION,
            correctOption = correctOption,
        )
    }

    @Test
    fun `processAnswer returns UnknownPoll when poll missing`() {
        val result = quizFlowService.processAnswer("missing", 0)
        assertTrue(result is AnswerResult.UnknownPoll)
    }

    @Test
    fun `processAnswer returns NextQuestion when lesson continues`() {
        registerPoll(questions = 2)
        whenever(userService.getOrCreate(123L)).thenReturn(user)
        stubWords()

        val result = quizFlowService.processAnswer("poll-1", 0)

        assertTrue(result is AnswerResult.NextQuestion)
        assertEquals(chatId, result.chatId)
        verify(wordAttemptService).recordAttempt(
            user = user,
            wordId = 1L,
            quizType = QuizType.WORD_TO_DEFINITION,
            correct = true,
        )
    }

    @Test
    fun `processAnswer returns Finished on last question`() {
        registerPoll(questions = 1)
        whenever(userService.getOrCreate(123L)).thenReturn(user)

        val result = quizFlowService.processAnswer("poll-1", 0)

        assertTrue(result is AnswerResult.Finished)
        assertEquals(1, result.score)
        assertEquals(WordConstraints.QUESTIONS_PER_LESSON, result.total)
    }

    @Test
    fun `processAnswer counts wrong answer in score`() {
        registerPoll(questions = 1, correctOption = 0)
        whenever(userService.getOrCreate(123L)).thenReturn(user)

        val result = quizFlowService.processAnswer("poll-1", 1) // wrong

        assertTrue(result is AnswerResult.Finished)
        assertEquals(0, result.score)
    }
}