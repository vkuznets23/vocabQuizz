package com.vocabquizzz.vocab_quizz.service

import org.springframework.stereotype.Service
import com.vocabquizzz.vocab_quizz.entity.Word
import com.vocabquizzz.vocab_quizz.entity.User
import com.vocabquizzz.vocab_quizz.repository.WordRepository
import com.vocabquizzz.vocab_quizz.repository.UserRepository
import org.springframework.transaction.annotation.Transactional

@Service
class WordService(
    private val wordRepository: WordRepository,
    private val userRepository: UserRepository
){

    fun getAllWords(user: User): List<Word> =
        wordRepository.findAllByUser(user)

    fun randomWord(user: User): Word =
        wordRepository.findAllByUser(user).random()

    fun addWord(
        user: User,
        word: String,
        definition: String,
        example: String?
    ): Word {

        if (wordRepository.existsByUserAndWord(user, word)) {
            throw IllegalArgumentException("Word already exists")
        }

        val newWord = Word(
            user = user,
            word = word,
            definition = definition,
            example = example
        )

        return wordRepository.save(newWord)
    }

    @Transactional
    fun deleteWord(
        user: User,
        word: String
    ): Boolean {
        return wordRepository.deleteByUserAndWord(user, word) > 0
    }
}