## Handlers

| Component             | Responsibility                                      |
| --------------------- | --------------------------------------------------- |
| **BotDispatcher**     | Routes Telegram updates to the appropriate handler. |
| **QuizHandler**       | Manages the quiz flow.                              |
| **AddWordHandler**    | Guides the user through adding a word.              |
| **EditWordHandler**   | Guides the user through editing a word.             |
| **DeleteWordHandler** | Deletes a word from the vocabulary.                 |
| **GetWordHandler**    | Displays information about a word.                  |
| **AllWordsHandler**   | Lists all saved words.                              |
| **HelpHandler**       | Displays available commands.                        |

## Services

| Component                  | Responsibility                  |
| -------------------------- | ------------------------------- |
| **QuizService**            | Generates quiz questions.       |
| **WordService**            | Manages vocabulary.             |
| **UserService**            | Retrieves or creates users.     |
| **LessonSessionService**   | Tracks active quiz sessions.    |
| **AddWordSessionService**  | Stores add-word session state.  |
| **EditWordSessionService** | Stores edit-word session state. |

## Infrastructure

| Component           | Responsibility                                     |
| ------------------- | -------------------------------------------------- |
| **TelegramGateway** | Sends messages and polls through the Telegram API. |
| **TelegramBot**     | Receives Telegram updates.                         |
| **UserRepository**  | Persists users.                                    |
| **WordRepository**  | Persists words.                                    |

## Flow

```
                    Telegram
                        │
                        ▼
              TelegramBot (Adapter)
                        │
                        ▼
               BotDispatcher (Router)
                        │
        ┌───────────────┼──────────────────────────────────┐
        ▼               ▼               ▼                  ▼
   QuizHandler    AddWordHandler  EditWordHandler   DeleteWordHandler
        │               │               │                  │
        ├───────────────┼───────────────┼──────────────────┤
        ▼               ▼               ▼                  ▼
 LessonSession   AddWordSession  EditWordSession     WordService
    Service         Service          Service              │
        │               │               │                 │
        └───────────────┴───────────────┴─────────────────┤
                                                          ▼
                                                    UserService
                                                          │
                                       ┌──────────────────┴──────────────────┐
                                       ▼                                     ▼
                               UserRepository                         WordRepository
                                       │                                     │
                                       └──────────────────┬──────────────────┘
                                                          ▼
                                                       Database
```
