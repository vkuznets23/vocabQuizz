INSERT INTO words (user_id, word, definition, example)
SELECT
    u.id,
    v.word,
    v.definition,
    v.example
FROM users u,
(
VALUES
('as if', 'used to show disbelief or say that something is impossible', '_____ I''d believe that story!'),
('pick apart', 'to analyze or criticize something in great detail', 'The professor ______ every argument in my essay.'),
('kiki', 'a casual chat or fun gathering with friends', 'Let''s have a little _____ over coffee this weekend.'),
('shell of a person', 'someone who has become emotionally or physically exhausted and is no longer like before', 'After working nonstop for months, he was just a _____.'),
('from the get-go', 'from the very beginning', 'I knew _____ that this project would be challenging'),
('blindsided', 'surprised or shocked by something unexpected', 'She was completely _____ by the companys decision'),
('gab', 'to talk a lot, often casually', 'We spent the whole evening _____ about our vacation plans'),
('dispel', 'to make a doubt, fear, or belief disappear', 'The doctor reassured me and _____ my fears'),
('ramification', 'a possible consequence or result of an action', 'You should think about the legal _____ before signing the contract'),
('get/have someone over a barrel', 'To put someone in a situation where they have no choice and can be taken advantage of', 'The only repair shop in town had me ____ , so I had to pay their high prices'),
('contender', 'A person or thing that has a good chance of winning a competition, contest, or achieving success.', 'After several victories, the boxer became a serious ____.'),
('dont hold back', 'To speak or act freely without being hesitant, restrained, or reserved.', 'If you have any feedback, ____.'),
('make up the time', 'To work extra hours or on other days to compensate for time missed from work.', 'I''m taking Friday off, but I''ll ____ next week.')
) AS v(word, definition, example)
WHERE u.telegram_user_id = 410458864;