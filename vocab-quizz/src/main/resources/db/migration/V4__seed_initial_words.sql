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
('have someone over a barrel', 'To put someone in a situation where they have no choice and can be taken advantage of', 'The only repair shop in town had me ____ , so I had to pay their high prices'),
('contender', 'A person or thing that has a good chance of winning a competition, contest, or achieving success.', 'After several victories, the boxer became a serious ____.'),
('dont hold back', 'To speak or act freely without being hesitant, restrained, or reserved.', 'If you have any feedback, ____.'),
('make up the time', 'To work extra hours or on other days to compensate for time missed from work.', 'I''m taking Friday off, but I''ll ____ next week.'),
('with flying color', 'to accomplish a task, or challenge very successfully and with high marks', 'she passed her final math exam _'),
('offhand', 'doing or saying smth without preparation, thought, or checking facts', 'i don’t know the price _'),
('to need a leg up', 'to need help, support', 'i’m struggling with physics and i really _ before the final exam next week'),
('stale', 'no longer fresh, new, or interesting', 'the bread gone ____'),
('divisive', 'causing a great disagreement. splitting people into opposite groups and creating discord', 'talking about politics at a family dinner can be a ____ choice that starts and argument'),
('pull it off', 'to successfully achieve or complete smth that is difficult, risky or unexpected', 'the plan is risky but i think we can ____'),
('compel', 'to force, drive or strongly urge someone to do smth', 'the court can ___ a witness to speak'),
('infuse', ' to fill or introduce a quality, feeling or substance into smth else', 'the coaches speech ____ the team with confidence'),
('byproduct', 'a secondary item made during making of smth else', 'wisdom is ____ of aging'),
('to zoom back', 'to change your perspective from a narrow focus to a big picture view', 'when you feel overwhelmed by minor argument _ and look at your entire relationship'),
('case by case basis', 'to decide on situation individually', 'a store might deny refunds normally but approve them in _ if an item was broken')
) AS v(word, definition, example)
WHERE u.telegram_user_id = 410458864;