# Application Flow

End-to-end flow for the Telegram vocab quiz bot.

## High-level architecture

```
Telegram
   │
   ▼
TelegramBot (adapter)
   │
   ▼
BotDispatcher (router)
   │
   ├── Handler (@Component)
   │      │
   │      ├── Service(s) — business logic
   │      │      └── Repository — database
   │      │
   │      ├── Formatter — messages / keyboards
   │      └── TelegramGateway — Telegram API
   │
   ├── PollAnswer → QuizHandler
   └── CallbackQuery → AllWordsCallbackHandler
```

---

## Routing (`BotDispatcher`)

| Update             | Route                                                                           |
| ------------------ | ------------------------------------------------------------------------------- |
| `/help`            | `HelpHandler`                                                                   |
| `/words`           | `AllWordsHandler`                                                               |
| `/word`            | `GetWordHandler`                                                                |
| `/quiz`            | `QuizHandler.handleQuiz`                                                        |
| `/add`             | `AddWordHandler.start`                                                          |
| `/edit`            | `EditWordHandler.start`                                                         |
| `/stat`            | `StatisticHandler`                                                              |
| `/delete`          | `DeleteWordHandler`                                                             |
| `/cancel`          | `CancelSessionHandler`                                                          |
| Plain text         | `AddWordHandler` or `EditWordHandler` if session active, else "Unknown command" |
| `PollAnswer`       | `QuizHandler.handlePoll`                                                        |
| Callback `words:N` | `AllWordsCallbackHandler`                                                       |

---

## Layers

| Layer      | Package         | Responsibility                                 |
| ---------- | --------------- | ---------------------------------------------- |
| Handler    | `bot.handler`   | Parse input, call services, format & send      |
| Formatter  | `bot.formatter` | User-facing text and inline keyboards          |
| Service    | `service`       | Business logic, validation, orchestration      |
| Model      | `model`         | DTOs, constraints, session state, result types |
| Repository | `repository`    | Database access                                |
| Gateway    | `bot.gateway`   | Telegram API                                   |

---

## Formatters

| Formatter               | Used by                                                   |
| ----------------------- | --------------------------------------------------------- |
| `HelpFormatter`         | `HelpHandler`                                             |
| `WordLookupFormatter`   | `GetWordHandler`, `DeleteWordHandler`, `StatisticHandler` |
| `AllWordsListFormatter` | `AllWordsHandler`, `AllWordsCallbackHandler`              |
| `StatisticFormatter`    | `StatisticHandler`                                        |
| `QuizFormatter`         | `QuizHandler`                                             |

---

## Services overview

### Domain (stateless, DB-backed)

| Service              | Responsibility                             |
| -------------------- | ------------------------------------------ |
| `WordService`        | CRUD, validation, `lookupWord`, pagination |
| `UserService`        | `getOrCreate(telegramUserId)`              |
| `StatisticService`   | Read quiz statistics                       |
| `WordAttemptService` | Write quiz attempts                        |

### Quiz

| Service                | Stateful? | Responsibility                                |
| ---------------------- | --------- | --------------------------------------------- |
| `QuizService`          | No        | Generate one `Quiz` from `Word` + distractors |
| `QuizDeckService`      | Yes       | Shuffled word deck per user                   |
| `QuizFlowService`      | No        | Quiz lifecycle orchestration                  |
| `LessonSessionService` | Yes       | Score, remaining questions, active polls      |

### Multi-step sessions

| Service                  | Responsibility           |
| ------------------------ | ------------------------ |
| `AddWordSessionService`  | Add-word wizard state    |
| `EditWordSessionService` | Edit-word wizard state   |
| `SessionCancelService`   | Cancel add / edit / quiz |

---

## Command flows

### `/help`

```
HelpHandler → HelpFormatter → TelegramGateway.sendText
```

### `/word <word>`

```
GetWordHandler
  → parse query from message (after "/word")
  → UserService.getOrCreate

If query is blank:
  → WordLookupFormatter.formatUsage("Usage: /word <word>")
  → TelegramGateway.sendText

Else:
  → WordService.lookupWord(user, query)
  → when (WordLookupResult):
       Found       → WordLookupFormatter.formatWord(word)       → sendText
       Suggestions → WordLookupFormatter.formatSuggestions(...)  → sendText
       NotFound    → WordLookupFormatter.formatNotFound(query)   → sendText
```

`lookupWord` logic (in `WordService`):

1. Exact match (case-insensitive) → `Found`
2. Else top-5 partial matches → `Suggestions`
3. Else → `NotFound`

### `/stat` and `/stat <word>`

Two branches in `StatisticHandler`:

```
/stat (no argument):
  StatisticHandler
    → StatisticService.getUserStatistic(user)
    → StatisticFormatter.formatUserStatistic(stat)
    → TelegramGateway.sendText

/stat <word>:
  StatisticHandler
    → parse query from message (after "/stat")
    → UserService.getOrCreate
    → WordService.lookupWord(user, query)
    → when (WordLookupResult):
         Found       → StatisticService.getStatisticForWord(word)
                    → StatisticFormatter.formatWordStatistic(stat)
                    → sendText
         Suggestions → WordLookupFormatter.formatSuggestions(...)  → sendText
         NotFound    → WordLookupFormatter.formatNotFound(query)   → sendText
```

Note: word stats use `lookup.word` directly — no second DB lookup for the word entity.

### `/delete <word>`

```
DeleteWordHandler
  → parse query from message (after "/delete")
  → UserService.getOrCreate

If query is blank:
  → WordLookupFormatter.formatUsage("Usage: /delete <word>")
  → TelegramGateway.sendText

Else:
  → WordService.lookupWord(user, query)
  → when (WordLookupResult):
       Found       → WordService.deleteWord(user, word)
                    → sendText "✅ '...' deleted."
       Suggestions → WordLookupFormatter.formatSuggestions(...)  → sendText
       NotFound    → WordLookupFormatter.formatNotFound(query)   → sendText
```

All three commands (`/word`, `/stat <word>`, `/delete`) share the same lookup outcomes via `WordLookupFormatter` for `Suggestions` and `NotFound`.

### `/words` and pagination

```
/words:
  AllWordsHandler
    → WordService.getWords(user, 0)
    → AllWordsListFormatter
    → TelegramGateway.sendTextWithKeyboard

Callback words:N:
  AllWordsCallbackHandler
    → WordService.getWords(user, N)
    → AllWordsListFormatter
    → TelegramGateway.editMessage
```

### `/add` (multi-step)

```
/add <word>
  → AddWordHandler.start
  → AddWordSessionService.start
  → WordService.ensureWordAvailable
  → prompt: definition → example (or /skip)

Each step:
  → WordService.validate* / addWord
  → on error: ❌ message, session stays open

/cancel → SessionCancelService → "❌ Add word cancelled."
```

### `/edit` (multi-step)

```
/edit <word>
  → EditWordHandler.start
  → WordService.getWord
  → EditWordSessionService.start
  → choose field (1/2/3) → new value (or /skip for example)

Each step:
  → WordService.updateWord
  → on error: ❌ message, session stays open

/cancel → SessionCancelService → "❌ Edit cancelled."
```

### `/cancel`

```
CancelSessionHandler
  → SessionCancelService.cancelAll(chatId)
  → CancelResult.message
  → TelegramGateway.sendText
```

Priority (one session per `/cancel`):

1. add word
2. edit word
3. quiz

---

## Quiz flow (detailed)

### Components

```
QuizHandler
  ↓
QuizFlowService
  ├── WordService          hasEnoughWordsForQuiz, getAllWords
  ├── QuizDeckService      resetDeck, nextWord
  ├── QuizService          generateQuiz(word, otherWords)
  ├── LessonSessionService startLesson, answer, finishLesson, polls
  ├── WordAttemptService   recordAttempt
  └── UserService          getOrCreate (in processAnswer)

QuizHandler also:
  ├── QuizFormatter        error / finished messages
  ├── TelegramGateway      sendQuiz, sendText
  └── LessonSessionService.registerPoll (after poll is sent)
```

### Result DTOs (`QuizFlowResult.kt`)

**StartQuizResult**

| Variant               | Meaning                               |
| --------------------- | ------------------------------------- |
| `Ready(chatId, quiz)` | Lesson started, first question ready  |
| `NotEnoughWords`      | Fewer than `MIN_WORDS_FOR_QUIZ` words |
| `AlreadyRunning`      | Quiz session already active           |

**AnswerResult**

| Variant                          | Meaning                         |
| -------------------------------- | ------------------------------- |
| `NextQuestion(chatId, quiz)`     | More questions remaining        |
| `Finished(chatId, score, total)` | Lesson complete                 |
| `UnknownPoll`                    | Stale or unknown poll — ignored |

### Start: `/quiz`

Steps:

1. `QuizHandler.handleQuiz` → `userService.getOrCreate`
2. `QuizFlowService.startQuiz`:
   - check word count → `NotEnoughWords`
   - `lessonSessionService.startLesson` → `AlreadyRunning`
   - `quizDeckService.resetDeck(user)`
   - `prepareNextQuestion`: deck → distractors → `generateQuiz`
   - return `Ready(chatId, quiz)`
3. Handler sends poll via `TelegramGateway`, then `registerPoll`

### Answer: poll button

Steps:

1. `finishPoll(pollId)` → match poll to user/word
2. `recordAttempt` → persist to DB
3. `answer` → update score, decrement remaining
4. If `remaining > 0` → next question; else → finish message

### Cancel during quiz

```
/cancel → SessionCancelService → lessonSessionService.cancelLesson
  - removes session
  - removes active polls for chatId
  - old poll buttons → UnknownPoll (silently ignored)
```

### Scheduled quiz

```
QuizScheduler (cron)
  → QuizHandler.startLesson(chatId)
  → same startQuiz flow as /quiz
```

---

## Domain constraints (`WordConstraints`)

| Constant                | Value |
| ----------------------- | ----- |
| `MIN_WORDS_FOR_QUIZ`    | 4     |
| `QUESTIONS_PER_LESSON`  | 5     |
| `MAX_WORDS_PER_PAGE`    | 10    |
| `MAX_WORD_LENGTH`       | 50    |
| `MAX_DEFINITION_LENGTH` | 200   |
| `MAX_EXAMPLE_LENGTH`    | 200   |

---

## Data flow to database

```
Write paths:
  /add, /edit     → WordService        → WordRepository
  Quiz answer     → WordAttemptService → WordAttemptRepository
  First message   → UserService        → UserRepository

Read paths:
  /word, /delete  → WordService.lookupWord
  /stat           → StatisticService   → WordAttemptRepository
  /words          → WordService.getWords (paginated)
  /quiz           → WordService.getAllWords (distractors)
```
