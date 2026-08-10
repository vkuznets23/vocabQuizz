package com.vocabquizzz.vocab_quizz.service.quiz

import org.springframework.stereotype.Service
import com.vocabquizzz.vocab_quizz.model.quiz.Quiz
import com.vocabquizzz.vocab_quizz.model.quiz.QuizType
import com.vocabquizzz.vocab_quizz.entity.Word
import kotlin.random.Random


@Service
class QuizService(

) {

    fun generateQuiz(word: Word, otherWords: List<Word>): Quiz {

        val quizzes = mutableListOf<(Word, List<Word>) -> Quiz>(
            ::wordToDefinitionQuiz,
            ::definitionToWordQuiz,
            ::trueFalseQuiz
        )

        if (word.example != null) {
            quizzes += ::sentenceQuiz
        }

        return quizzes.random()(word, otherWords)
    }

    private fun definitionToWordQuiz(word: Word, otherWords: List<Word>): Quiz {
        val wrong = otherWords
            .shuffled()
            .take(3)
            .map { it.word }

        val options = (wrong + word.word).shuffled()

        return Quiz(
            question = word.definition,
            options = options,
            correct = word.word,
            type = QuizType.DEFINITION_TO_WORD,
            wordId = word.id
        )

    }

    private fun wordToDefinitionQuiz(word: Word, otherWords: List<Word>): Quiz {
        val wrong = otherWords
            .shuffled()
            .take(3)
            .map { it.definition }

        val options = (wrong + word.definition).shuffled()

        return Quiz(
            question = "What does '${word.word}' mean?",
            options = options,
            correct = word.definition,
            type = QuizType.WORD_TO_DEFINITION,
            wordId = word.id
        )
    }

    private fun trueFalseQuiz(word: Word, otherWords: List<Word>): Quiz {
        val isTrue = Random.nextBoolean()

        val definition = 
            if (isTrue) {
                word.definition
            } else {
                otherWords.random().definition
            }
        
        return Quiz(
            question = """
                True or False?

                ${word.word}
                $definition
            """.trimIndent(),
            options = listOf("True", "False"),
            correct = if (isTrue) "True" else "False",
            type = QuizType.TRUE_FALSE,
            wordId = word.id
        )
    }

    private fun sentenceQuiz(word: Word, otherWords: List<Word>): Quiz {
        val wrong = otherWords
            .shuffled()
            .take(3)
            .map { it.word }

        val options = (wrong + word.word).shuffled()

        return Quiz(
            question = word.example ?: "",
            options = options,
            correct = word.word,
            type = QuizType.SENTENCE,
            wordId = word.id
        )
    }
}