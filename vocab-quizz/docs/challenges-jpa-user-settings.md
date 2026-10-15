# JPA Challenges: User ↔ Quiz Settings (One-to-One)

Two related problems we hit while adding `user_quiz_settings` and wiring it into `/quiz`, `/settings`, and the scheduler. Same domain area — one user, one settings row — different JPA footguns.

**Related code:** `UserQuizSettings.kt`, `UserService.kt`, `UserQuizSettingsRepository.kt`, `QuizScheduler.kt`  
**See also:** `database.md`, `flow.md`

---

## Context

We added per-user quiz preferences:

| Table                | Role                                                                    |
| -------------------- | ----------------------------------------------------------------------- |
| `users`              | Who this person is (`telegram_user_id`)                                 |
| `user_quiz_settings` | How quizzes work for them (auto-quiz, interval, window, question count) |

- **One user → one settings row**
- PK of settings = `user_id` (FK to `users.id`)
- JPA: `@OneToOne` + `@MapsId` on `UserQuizSettings.user`

Settings always belong to someone — the relationship is required. But `@OneToOne` in JPA is easy to get wrong in two opposite ways: saving too much on insert, loading too little on read.

### When settings rows are created

We chose **create on first registration** (Option A):

```kotlin
// UserService.getOrCreate — @Transactional
val user = repository.save(User(telegramUserId = ...))
settingsRepository.save(UserQuizSettings(user = repository.getReferenceById(user.id)))
```

| Approach                                           | Pros                                                       | Cons                                             |
| -------------------------------------------------- | ---------------------------------------------------------- | ------------------------------------------------ |
| **A: Create with user** (chosen)                   | Every user always has settings; no null checks in handlers | Must backfill existing users (V6 migration)      |
| **B: Lazy create on first `/settings` or `/quiz`** | No backfill migration                                      | `getSettings` can throw; extra branch everywhere |

---

## Challenge 1: Detached `User` on first settings insert

### The sticking point

> "I'm saving **settings**, why is Hibernate trying to persist **User** again?"

When we first wired settings into `/quiz`, the bot went silent.

**Flow:**

1. Handler calls `userService.getOrCreate()` → `User` loaded from DB
2. Transaction **ends**
3. Code tries `save(UserQuizSettings(user = user))` with that same `User` instance
4. Hibernate sees a `User` attached to a new entity
5. It may cascade / merge → **detached entity passed to persist** (user already exists)

The bug: we passed a **detached** `User` object (loaded in a closed session), not a FK reference.

### Options we considered

| Option                                            | Idea                                                      | Why we didn't pick it                                                        |
| ------------------------------------------------- | --------------------------------------------------------- | ---------------------------------------------------------------------------- |
| **`getReferenceById`** (chosen)                   | Pass a lazy FK stub: "user #5 exists, only set `user_id`" | —                                                                            |
| **`@Transactional` on settings create**           | Keep same session so `User` stays managed                 | Couples callers to transaction boundaries; easy to break again from handlers |
| **Set only `userId` field, skip `user` relation** | Avoid touching `User` entity                              | Awkward with `@MapsId` / `@OneToOne`; fights the mapping                     |
| **`merge(user)` before save**                     | Re-attach detached user                                   | Unnecessary write path; still confusing ownership                            |
| **Duplicate `telegram_user_id` on settings**      | No JPA link to `User` on insert                           | Denormalization; sync risk (see Challenge 2)                                 |

### What we did

Use a **reference** to an existing row, not the detached instance:

```kotlin
UserQuizSettings(user = repository.getReferenceById(user.id))
```

Meaning: "Settings belong to user #5. Don't INSERT into `users` — only set `user_id = 5`."

**Where this happens:** once in `UserService.getOrCreate`. Everywhere else (`setEnabled`, `setInterval`, …) → load settings, mutate fields, `save(settings)`. User is never saved again.

### Mental model

| Operation       | Touch                                                  |
| --------------- | ------------------------------------------------------ |
| Update settings | `user_quiz_settings` only                              |
| Create settings | New row + FK to existing user — **not** a new user row |

### Lesson

One-to-one link = **who owns this row**, not **save both tables every time**.

---

## Challenge 2: `LazyInitializationException` in the scheduler

### The sticking point

> "Settings loaded fine — why does `settings.user.telegramUserId` crash?"

```kotlin
// QuizScheduler
settings.user.telegramUserId  // 💥 LazyInitializationException
```

We use `FetchType.LAZY` on `UserQuizSettings.user`:

```kotlin
@OneToOne(fetch = FetchType.LAZY)
@MapsId
@JoinColumn(name = "user_id")
val user: User
```

**Why LAZY:** Most handlers already have `User` from `getOrCreate` and only read settings fields (`enabled`, `questionsPerLesson`, …). LAZY avoids JOINing `users` on every settings query.

**What happened in the scheduler:**

1. `findEnabled()` loads `UserQuizSettings` list
2. `user` is a **lazy proxy** — not fetched yet
3. Repository transaction **ends**
4. `QuizScheduler` reads `settings.user.telegramUserId` **outside** the session → crash

Settings looked loaded; the related `User` was not.

### Options we considered

| Option                                             | Idea                                  | Why we didn't pick it                                                                              |
| -------------------------------------------------- | ------------------------------------- | -------------------------------------------------------------------------------------------------- |
| **`@EntityGraph` on specific queries** (chosen)    | Eager-fetch `user` only where needed  | —                                                                                                  |
| **`FetchType.EAGER` on `user`**                    | Always load user with settings        | Extra JOIN on every `/settings`, `/quiz`, `/help` — wasteful when `User` is already in the handler |
| **Store `telegram_user_id` on settings**           | Scheduler reads chatId without `user` | Duplicate of `users.telegram_user_id`; drift risk                                                  |
| **`JOIN FETCH` in custom `@Query`**                | Same as EntityGraph, manual JPQL      | Works, but EntityGraph is declarative and lives on the repository method                           |
| **`@Transactional` on scheduler method**           | Keep session open for whole loop      | Hides the problem; long transactions; N+1 if each `user` loads separately                          |
| **Pass `userId` to `UserRepository` in scheduler** | Second query per eligible user        | Extra round-trips; EntityGraph loads in one query                                                  |

### What we did

`@EntityGraph` on the one query that needs `user`:

```kotlin
@EntityGraph(attributePaths = ["user"])
fun findByEnabledTrue(): List<UserQuizSettings>
```

One query: settings + user. LAZY stays the default everywhere else.

**Scheduler usage:**

```kotlin
settingsService.findEnabled()
    .filter { settingsService.isEligibleNow(it) }
    .forEach { settings ->
        val chatId = settings.user.telegramUserId.toString()
        if (quizHandler.startLesson(chatId)) {
            settingsService.markQuizSent(settings)
        }
    }
```

In private Telegram chats, `chatId == telegramUserId`. We still keep a single source of truth in `users` — no extra column on settings.

### When things work vs break

```
findByUserId / getSettings (handlers)
─────────────────────────────────────
SELECT user_quiz_settings only
user = proxy (not loaded) ✅ — settings fields are enough

findByEnabledTrue + @EntityGraph (scheduler)
────────────────────────────────────────────
SELECT settings JOIN user
user.telegramUserId available ✅

findByUserId + settings.user.* outside transaction
──────────────────────────────────────────────────
💥 LazyInitializationException
```

### Lesson

**LAZY by default; eager-load `user` only on queries that need it.**

---

## Summary

| #   | Symptom                             | Root cause                               | Fix                                                |
| --- | ----------------------------------- | ---------------------------------------- | -------------------------------------------------- |
| 1   | Bot silent on first settings insert | Detached `User` passed to `save()`       | `getReferenceById(user.id)` when creating settings |
| 2   | Scheduler crash on `settings.user`  | LAZY proxy accessed after session closed | `@EntityGraph(["user"])` on `findByEnabledTrue()`  |

Both challenges come from the same `@OneToOne`: you must express **ownership** without over-saving on write, and **navigation** without over-loading on read. Pick the tool per use case — reference on insert, EntityGraph on the one read path that crosses the boundary.
