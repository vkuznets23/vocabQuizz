# Database Design

## Overview

At the first stage of the Telegram bot, we need a simple database structure to store:

- Telegram users;
- each user's personal vocabulary (words);
- quiz answer history;
- per-user quiz settings (auto-quiz schedule, question count).

Main relationships:

- one user → many words;
- one user → many word attempts;
- one user → one quiz settings row (optional until first `/settings` or `/quiz`).

<img width="685" height="304" alt="image" src="https://github.com/user-attachments/assets/ada4574f-a975-4069-848a-7a3407506814" />

## Migrations

| Version | File                                 | Description          |
| ------- | ------------------------------------ | -------------------- |
| V1      | `V1__create_users_table.sql`         | `users`              |
| V2      | `V2__create_words_table.sql`         | `words`              |
| V3      | `V3__create_words_attempt_table.sql` | `word_attempts`      |
| V4      | `V4__seed_initial_words.sql`         | seed data            |
| V5      | `V5__create_user_quiz_settings.sql`  | `user_quiz_settings` |

---

# Tables

## users

The `users` table stores Telegram users. Minimum info to identify users and link their data.

```dbml
TABLE users {
    id integer [primary key]
    telegram_user_id integer [unique, not null]
    created_at timestamp [not null, default: `now()`]
}
```

## words

The words table stores users' personal vocabulary.

Each word belongs to a specific user.

```dbml
Table words {
  id bigint [pk, increment]
  user_id bigint [not null, ref: > users.id]
  word varchar(50) [not null]
  definition text [not null]
  example text
  created_at timestamp [not null, default: `now()`]

  indexes {
    (user_id, word) [unique]
    (user_id, lower(word)) [unique]
  }
}
```

## word_attempts

Quiz answer history. One row per poll answer.

```dbml
Table word_attempts {
  id bigint [pk, increment]
  word_id bigint [not null, ref: > words.id]
  user_id bigint [not null, ref: > users.id]
  is_correct boolean [not null]
  quiz_type varchar(50) [not null]
  created_at timestamp [not null, default: `now()`]
}
```

## user_quiz_settings

# Future Extensions

Possible future improvements:

word_attempts — stores quiz answer history;
decks — allows grouping words into collections;
statistics — provides learning analytics;
next_review — supports spaced repetition learning algorithms.
