package com.vocabquizzz.vocab_quizz.bot

import org.springframework.stereotype.Component
import com.vocabquizzz.vocab_quizz.bot.handler.QuizHandler
import com.vocabquizzz.vocab_quizz.bot.handler.AddWordHandler
import com.vocabquizzz.vocab_quizz.service.AddWordSessionService
import com.vocabquizzz.vocab_quizz.gateway.TelegramGateway
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer

@Component
class BotDispatcher(
    private val quizHandler: QuizHandler,
    private val telegramGateway: TelegramGateway,
    private val addWordSessionService: AddWordSessionService,
    private val addWordHandler: AddWordHandler

) {
    fun dispatch(message: Message) {

        when (message.text?.substringBefore(" ")) {

            "/quiz" ->
                quizHandler.handleQuiz(message)
            
            "/add" -> 
                addWordHandler.handle(message)
            
            else -> {

                val chatId = message.chatId.toString()

                if (addWordSessionService.isRunning(chatId)) {
                    addWordHandler.handle(message)
                } else {
                    telegramGateway.sendText(
                        chatId,
                        "Unknown command"
                    )
                }
            }
        }
    }

    fun dispatch(answer: PollAnswer) {
        quizHandler.handlePoll(answer)
    }
}