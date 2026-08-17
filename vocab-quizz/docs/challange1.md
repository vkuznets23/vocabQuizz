# Challenge: User ↔ Quiz Settings (One-to-One)

## The setup

We added a new table: `user_quiz_settings`.

- **One user** → **one settings row**
- Linked by `user_id` (settings PK = user's id)
- In JPA: `@OneToOne` between `User` and `UserQuizSettings`

We need this link because settings don't make sense without a user:

- Who turned auto-quiz ON?
- How many questions should _this_ person get?
- What's _their_ time window?

So the relationship is **required**.

But `@OneToOne` in JPA comes with surprises.

## Challenge 1: "I only want to save settings, why is Hibernate touching User?"

### What happened

When we first wired settings into `/quiz`, the bot went silent.

Flow:

1. Handler calls `userService.getOrCreate()` → User loaded from DB
2. That transaction **ends**
3. Code tries to **create** settings: `save(UserQuizSettings(user = user))`
4. Hibernate sees a `User` object attached to new settings
5. It tries to **persist User again** → crash (User already exists)

### Why it's confusing

We thought: "I'm saving **settings**, not user!"

But JPA sees user associated with UserQuizSettings. When saving a **new** settings row, Hibernate may cascade to the linked `User`. If that `User` is **detached** (outside an active session), you get the error.

### The mental model

| Table                | Role                                 |
| -------------------- | ------------------------------------ |
| `users`              | Who is this person (Telegram id)     |
| `user_quiz_settings` | How quizzes work **for** that person |

When **updating** settings → only touch `user_quiz_settings`.

When **creating** settings → you must say _which user_ they belong to — but that does **not** mean "save user again".

### How we fixed it

Use a **reference** to an existing user, not the detached object:

```kotlin
UserQuizSettings(user = userRepository.getReferenceById(user.id))
```

Meaning: "Settings belong to user #5. User #5 already exists. Don't INSERT user — only set user_id = 5."

### Where we create settings now

Once, when the user is first registered:

```kotlin
// UserService.getOrCreate — @Transactional
val user = repository.save(User(...))
settingsRepository.save(
    UserQuizSettings(user = repository.getReferenceById(user.id))
)
```

Everywhere else (setEnabled, setInterval, etc.) → load settings, change fields, save(settings). User is not saved.

### Lesson

One-to-one link = "who owns this row", not "save both tables every time".
