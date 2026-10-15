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
- one user → one quiz settings row (created on first registration).

<img width="685" height="304" alt="image" src="https://github.com/user-attachments/assets/ada4574f-a975-4069-848a-7a3407506814" />

## Migrations

| Version | File                                  | Description                          |
| ------- | ------------------------------------- | ------------------------------------ |
| V1      | `V1__create_users_table.sql`          | `users`                              |
| V2      | `V2__create_words_table.sql`          | `words`                              |
| V3      | `V3__create_words_attempt_table.sql`  | `word_attempts`                      |
| V4      | `V4__seed_initial_words.sql`          | seed data                            |
| V5      | `V5__create_user_quiz_settings.sql`   | `user_quiz_settings`                 |
| V6      | `V6__backfill_user_quiz_settings.sql` | backfill settings for existing users |

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

Per-user quiz preferences: auto-quiz on/off, send interval, daily time window, timezone, and question count per session.

Each user has exactly one settings row. Primary key is `user_id` (also FK to `users.id`, `ON DELETE CASCADE`).

Rows are created in `UserService.getOrCreate` when a new Telegram user registers. Migration V6 backfills settings for users who existed before V5.

Used by:

- `/settings` — read and update preferences;
- `/quiz`, `/help` — `questions_per_lesson` for session size;
- scheduler — `enabled`, interval, window, timezone, `last_quiz_sent_at` for eligibility.

```dbml
Table user_quiz_settings {
  user_id bigint [pk, ref: > users.id]
  enabled boolean [not null, default: false]
  interval_hours int [not null, default: 2, note: '1–3 hours']
  window_start time [not null, default: '09:00']
  window_end time [not null, default: '18:00']
  timezone varchar(64) [not null, default: 'UTC']
  questions_per_lesson int [not null, default: 5, note: '5–10 questions']
  last_quiz_sent_at timestamp [null]
  updated_at timestamp [not null, default: `now()`]
}
```

| Column                 | Default           | Meaning                                             |
| ---------------------- | ----------------- | --------------------------------------------------- |
| `enabled`              | `false`           | Whether the scheduler sends auto-quizzes            |
| `interval_hours`       | `2`               | Minimum hours between auto-quizzes (1–3)            |
| `window_start` / `end` | `09:00` – `18:00` | Local time range when auto-quizzes may be sent      |
| `timezone`             | `UTC`             | IANA timezone for window and eligibility checks     |
| `questions_per_lesson` | `5`               | Poll count per `/quiz` or auto-quiz session (5–10)  |
| `last_quiz_sent_at`    | `null`            | Last auto-quiz timestamp; `null` means eligible now |
| `updated_at`           | `now()`           | Last settings change                                |

# Future Extensions

Possible future improvements:

decks — allows grouping words into collections;
somehow make it to support spaced repetition learning algorithms.
