ALTER TABLE words ADD COLUMN deck_id BIGINT NULL;

INSERT INTO decks (user_id, name)
SELECT u.id, 'General'
FROM users u
WHERE NOT EXISTS (
    SELECT 1
    FROM decks d
    WHERE d.user_id = u.id
      AND LOWER(d.name) = 'general'
);

UPDATE words w
SET deck_id = d.id
FROM decks d
WHERE d.user_id = w.user_id
  AND LOWER(d.name) = 'general';

ALTER TABLE words
    ADD CONSTRAINT fk_words_deck
        FOREIGN KEY (deck_id) REFERENCES decks(id) ON DELETE CASCADE;
ALTER TABLE words
    ALTER COLUMN deck_id SET NOT NULL;

ALTER TABLE words DROP CONSTRAINT uq_user_word;
DROP INDEX IF EXISTS uq_words_user_word_lower;

CREATE UNIQUE INDEX uq_words_deck_word_lower
    ON words (deck_id, LOWER(word));