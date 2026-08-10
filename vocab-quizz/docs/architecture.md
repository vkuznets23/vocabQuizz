# Application Flow

Layered architecture for the Telegram vocab quiz bot.

## Layers

| Layer          | Package         | Responsibility                                              |
| -------------- | --------------- | ----------------------------------------------------------- |
| **Handler**    | `bot.handler`   | Parse Telegram input, call services, format & send response |
| **Formatter**  | `bot.formatter` | User-facing text and inline keyboards (no business logic)   |
| **Service**    | `service`       | Business logic, validation, orchestration                   |
| **Model**      | `model`         | DTOs, constraints, session state, result types              |
| **Repository** | `repository`    | Database access                                             |
| **Gateway**    | `bot.gateway`   | Telegram API (messages, polls, callbacks)                   |

Handlers are `@Component`. Services are `@Service`.

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

## Services

### Domain (stateless, DB-backed)

| Service              | Responsibility                                            |
| -------------------- | --------------------------------------------------------- |
| `WordService`        | CRUD, validation, lookup (`WordLookupResult`), pagination |
| `UserService`        | `getOrCreate(telegramUserId)`                             |
| `StatisticService`   | Read quiz statistics                                      |
| `WordAttemptService` | Write quiz attempts only                                  |

### Quiz (split by concern)

| Service                | Stateful?       | Responsibility                                |
| ---------------------- | --------------- | --------------------------------------------- |
| `QuizService`          | No              | Generate one `Quiz` from `Word` + distractors |
| `QuizDeckService`      | Yes (in-memory) | Shuffled word deck per user                   |
| `QuizFlowService`      | No              | Quiz lifecycle orchestration                  |
| `LessonSessionService` | Yes (in-memory) | Score, remaining questions, active polls      |

### Sessions (multi-step flows)

| Service                  | Responsibility                            |
| ------------------------ | ----------------------------------------- |
| `AddWordSessionService`  | Add-word wizard state                     |
| `EditWordSessionService` | Edit-word wizard state                    |
| `SessionCancelService`   | Cancel add / edit / quiz (priority order) |

---

## Command flows

### `/help`

HelpHandler → HelpFormatter → TelegramGateway.sendText

### `/word`, `/delete`, `/stat <word>`

Handler → WordService.lookupWord() → WordLookupFormatter / StatisticFormatter → TelegramGateway.sendText

### `/stat` (no argument)

StatisticHandler → StatisticService.getUserStatistic() → StatisticFormatter → TelegramGateway.sendText

### `/words` + pagination callbacks

words: AllWordsHandler → WordService.getWords(user, 0) → AllWordsListFormatter → TelegramGateway.sendTextWithKeyboard

words:N callback: AllWordsCallbackHandler → WordService.getWords(user, N) → AllWordsListFormatter → TelegramGateway.editMessage

### `/add`, `/edit` (multi-step)

Handler.start → SessionService.start() User text → Handler.handle → WordService (validate / save) Validation error → stay in session, send ❌ message /cancel → SessionCancelService.cancelAll()

### `/cancel`

CancelSessionHandler → SessionCancelService.cancelAll() → CancelResult.message → TelegramGateway.sendText

Priority: add → edit → quiz (one session per /cancel)
