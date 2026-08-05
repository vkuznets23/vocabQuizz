```
                                    Telegram
                                        │
                                        ▼
                                TelegramBot (Adapter)
                                        │
                                        ▼
                                  BotDispatcher (Router)
                           ┌────────────┴────────────┐
                           │                         │
                           ▼                         ▼
                        Message                   PollAnswer
                           │                         │
                           └────────────┬────────────┘
                                        ▼
                                   QuizHandler
                              ┌─────────┼──────────┬────────────┐
                              ▼         ▼          ▼            ▼
                       LessonService UserService QuizService TelegramGateway
                                        │          │
                                        ▼          ▼
                                   UserRepository WordRepository
                                   │               │
                                   └───────┬───────┘
                                           ▼
                                        Database
```

```
Telegram
    │
    ▼
TelegramBot
    │
    ▼
BotDispatcher
    │
    ├──────────────────────────────────────────┐
    │                                          │
    ▼                                          ▼
dispatch(Message)                    dispatch(PollAnswer)
    │                                          │
    ▼                                          ▼
QuizHandler.handle(message)         QuizHandler.handle(answer)
    │                                          │
    ▼                                          ▼
startLesson()                    lessonService.finishPoll()
    │                                          │
    ▼                                          ▼
lessonService.startLesson()         lessonService.answer()
    │                                          │
    ▼                                          ▼
sendQuiz()                                 hasNext ?
    │                                    ┌─────┴──────┐
    ▼                                    │            │
userService.getOrCreate()                ▼            ▼
    │                               sendQuiz()  finishLesson()
    ▼                                    │            │
quizService.getQuiz()                    │            ▼
    │                                    │   lessonService.finishLesson()
    ▼                                    │            │
telegramGateway.sendQuiz()               │            ▼
    │                                    │ telegramGateway.sendText()
    ▼                                    │
lessonService.registerPoll() ◄───────────┘
```
