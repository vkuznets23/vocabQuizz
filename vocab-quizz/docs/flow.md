```
Telegram
    │
    ▼
TelegramBot.consume()
    │
    ▼
BotService
    │
    ├──────────────────────────────────────────┐
    │                                          │
    ▼                                          ▼
handleMessage()                         handlePollAnswer()
    │                                          │
    ▼                                          ▼
startLesson()                     lessonService.finishPoll()
    │                                          │
    ▼                                          ▼
lessonService.startLesson()          lessonService.answer()
    │                                          │
    ▼                                          ▼
sendQuiz()                             hasNext ?
    │                                 ┌────┴─────┐
    ▼                                 │          │
userService.getOrCreate()             ▼          ▼
    │                             sendQuiz()  finishLesson()
    ▼                                 │          │
quizService.getQuiz()                 │          ▼
    │                                 │   lessonService.finishLesson()
    ▼                                 │          │
telegramGateway.sendQuiz()            │          ▼
    │                                 │ telegramGateway.sendText()
    ▼                                 │
lessonService.registerPoll()◄─────────┘
```
