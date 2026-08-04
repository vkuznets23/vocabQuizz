package com.vocabquizzz.vocab_quizz.service

import org.springframework.stereotype.Service
import com.vocabquizzz.vocab_quizz.entity.Word
import com.vocabquizzz.vocab_quizz.entity.User
import com.vocabquizzz.vocab_quizz.repository.WordRepository
import com.vocabquizzz.vocab_quizz.repository.UserRepository


@Service
class WordService(
    private val wordRepository: WordRepository,
    private val userRepository: UserRepository
){

    fun getAllWords(user: User): List<Word> =
        wordRepository.findAllByUser(user)

    fun randomWord(user: User): Word =
        wordRepository.findAllByUser(user).random()
}