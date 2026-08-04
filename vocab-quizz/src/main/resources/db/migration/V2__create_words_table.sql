CREATE TABLE words (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    word VARCHAR(50) NOT NULL,
    definition TEXT NOT NULL,
    example TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_words_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_user_word
        UNIQUE (user_id, word)
);