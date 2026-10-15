CREATE TABLE user_quiz_settings (
    user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,

    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    interval_hours INT NOT NULL DEFAULT 2 CHECK (interval_hours BETWEEN 1 AND 3),
    window_start TIME NOT NULL DEFAULT '09:00',
    window_end TIME NOT NULL DEFAULT '18:00',
    timezone VARCHAR(64) NOT NULL DEFAULT 'UTC',
    questions_per_lesson INT NOT NULL DEFAULT 5 CHECK (questions_per_lesson BETWEEN 5 AND 10),

    last_quiz_sent_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);