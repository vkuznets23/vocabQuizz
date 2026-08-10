package com.vocabquizzz.vocab_quizz.service.quiz

import org.springframework.stereotype.Service
import com.vocabquizzz.vocab_quizz.model.StartQuizResult
import com.vocabquizzz.vocab_quizz.model.AnswerResult
import com.vocabquizzz.vocab_quizz.entity.User
import com.vocabquizzz.vocab_quizz.model.Quiz
import com.vocabquizzz.vocab_quizz.service.word.WordService
import com.vocabquizzz.vocab_quizz.service.quiz.QuizService
import com.vocabquizzz.vocab_quizz.service.user.UserService
import com.vocabquizzz.vocab_quizz.service.quiz.QuizDeckService
import com.vocabquizzz.vocab_quizz.service.quiz.LessonSessionService
import com.vocabquizzz.vocab_quizz.service.word.WordAttemptService
import com.vocabquizzz.vocab_quizz.model.WordConstraints    


@Service
class QuizFlowService(
    private val wordService: WordService,
    private val quizService: QuizService,
    private val userService: UserService,
    private val quizDeckService: QuizDeckService,
    private val lessonSessionService: LessonSessionService,
    private val wordAttemptService: WordAttemptService,
) {

    fun startQuiz(chatId: String, user: User): StartQuizResult {
        if (!wordService.hasEnoughWordsForQuiz(user)) {
            return StartQuizResult.NotEnoughWords
        }

        if (!lessonSessionService.startLesson(chatId)) {
            return StartQuizResult.AlreadyRunning
        }

        quizDeckService.resetDeck(user)

        val quiz = prepareNextQuestion(user)
        return StartQuizResult.Ready(chatId, quiz)
    }

    private fun prepareNextQuestion(user: User): Quiz {
        val word = quizDeckService.nextWord(user)
        val otherWords = wordService.getAllWords(user).filter { it.id != word.id }
        return quizService.generateQuiz(word, otherWords)
    }

    fun processAnswer(pollId: String, selectedOption: Int): AnswerResult {
        val activePoll = lessonSessionService.finishPoll(pollId)
            ?: return AnswerResult.UnknownPoll

        val correct = selectedOption == activePoll.correctOption
        val chatId = activePoll.chatId
        val user = userService.getOrCreate(chatId.toLong())

        wordAttemptService.recordAttempt(
            user = user,
            wordId = activePoll.wordId,
            quizType = activePoll.quizType,
            correct = correct,
        )

        val hasNext = lessonSessionService.answer(chatId, correct)

        return if (hasNext) {
            AnswerResult.NextQuestion(chatId, prepareNextQuestion(user))
        } else {
            val session = lessonSessionService.finishLesson(chatId)
                ?: return AnswerResult.UnknownPoll
            AnswerResult.Finished(
                chatId = chatId,
                score = session.score,
                total = WordConstraints.QUESTIONS_PER_LESSON,
            )
        }
    }

}