# Database Design

## Overview

At the first stage of the Telegram bot, we need a simple database structure to store:

- Telegram users;
- each user's personal vocabulary;
- words with definitions;
- examples of word usage.

The main idea:

- one user can have many words;
- each word belongs to exactly one user.

<img width="685" height="304" alt="image" src="https://github.com/user-attachments/assets/ada4574f-a975-4069-848a-7a3407506814" />


# Tables

## users

The `users` table stores Telegram users.

It contains the minimum information required for the bot to identify users and store their data.

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
TABLE words {
    id integer [primary key]
    user_id integer [not null]
    word varchar(50) [not null]
    definition varchar(200) [not null]
    example varchar
    created_at timestamp [not null, default: `now()`]
}
```

# Future Extensions

Possible future improvements:

word_attempts — stores quiz answer history;
decks — allows grouping words into collections;
statistics — provides learning analytics;
next_review — supports spaced repetition learning algorithms.
