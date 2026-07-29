package com.vocabquizzz.vocab_quizz

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableScheduling
class VocabQuizzApplication

fun main(args: Array<String>) {
	runApplication<VocabQuizzApplication>(*args)
}
