CREATE TABLE word_attempts (
    id BIGSERIAL PRIMARY KEY,
    word_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    is_correct BOOLEAN NOT NULL,
    quiz_type VARCHAR(50) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_word_attempts_word
        FOREIGN KEY (word_id)
        REFERENCES words(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_word_attempts_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);