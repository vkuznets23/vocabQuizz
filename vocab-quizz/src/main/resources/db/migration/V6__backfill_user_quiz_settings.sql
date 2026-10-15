INSERT INTO user_quiz_settings (user_id)
SELECT id FROM users u
WHERE NOT EXISTS (
    SELECT 1 FROM user_quiz_settings s WHERE s.user_id = u.id
);