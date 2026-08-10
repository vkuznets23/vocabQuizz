package com.vocabquizzz.vocab_quizz.bot

import org.springframework.stereotype.Component
import com.vocabquizzz.vocab_quizz.bot.handler.HelpHandler
import com.vocabquizzz.vocab_quizz.bot.handler.AllWordsCallbackHandler
import com.vocabquizzz.vocab_quizz.bot.handler.GetWordHandler
import com.vocabquizzz.vocab_quizz.bot.handler.AllWordsHandler
import com.vocabquizzz.vocab_quizz.bot.handler.QuizHandler
import com.vocabquizzz.vocab_quizz.bot.handler.AddWordHandler
import com.vocabquizzz.vocab_quizz.bot.handler.DeleteWordHandler
import com.vocabquizzz.vocab_quizz.bot.handler.CancelSessionHandler
import com.vocabquizzz.vocab_quizz.bot.handler.EditWordHandler
import com.vocabquizzz.vocab_quizz.bot.handler.StatisticHandler
import com.vocabquizzz.vocab_quizz.service.session.AddWordSessionService
import com.vocabquizzz.vocab_quizz.service.session.EditWordSessionService
import com.vocabquizzz.vocab_quizz.bot.gateway.TelegramGateway
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer
import org.telegram.telegrambots.meta.api.objects.CallbackQuery


@Component
class BotDispatcher(
    private val helpHandler: HelpHandler,
    private val allWordsHandler: AllWordsHandler,
    private val getWordHandler: GetWordHandler,
    private val quizHandler: QuizHandler,
    private val telegramGateway: TelegramGateway,
    private val addWordSessionService: AddWordSessionService,
    private val editWordSessionService: EditWordSessionService,
    private val addWordHandler: AddWordHandler,
    private val statisticHandler: StatisticHandler,
    private val deleteWordHandler: DeleteWordHandler,
    private val cancelSessionHandler: CancelSessionHandler,
    private val editWordHandler: EditWordHandler,
    private val allWordsCallbackHandler: AllWordsCallbackHandler,

) {
    fun dispatch(message: Message) {

        when (message.text?.substringBefore(" ")) {

            "/help" ->
                helpHandler.handle(message)

            "/words" ->
                allWordsHandler.handle(message)
            
            "/word" ->
                getWordHandler.handle(message)

            "/quiz" ->
                quizHandler.handleQuiz(message)
            
            "/add" -> 
                addWordHandler.start(message)

            "/edit" ->
                editWordHandler.start(message)
            
            "/stat" ->
                statisticHandler.handle(message)
            
            "/delete" ->
                deleteWordHandler.handle(message)
            
            "/cancel" ->
                cancelSessionHandler.handle(message)
            
            else -> {
                val chatId = message.chatId.toString()

                if (addWordSessionService.isRunning(chatId)) {
                    addWordHandler.handle(message)
                } else if (editWordSessionService.isRunning(chatId)) {
                    editWordHandler.handle(message)
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

    fun dispatch(callback: CallbackQuery) {

        when {
            callback.data.startsWith("words:") ->
                allWordsCallbackHandler.handle(callback)
        }
    }
}