INSERT INTO APP_USER(user_id, email, name, password, username) VALUES (3, 'jane_doe@gmail.com', 'Jane Doe', 'hashedpassword', 'jane_girl18');
INSERT INTO APP_USER(user_id, email, name, password, username) VALUES (5, 'joao_silva12@gmail.com', 'Joao Silva', 'hashedpassword2', 'joaozinn');
INSERT INTO APP_USER(user_id, email, name, password, username) VALUES (6, 'thirdwheel@gmail.com', 'John Doe', 'hashedpassword3', 'third_wheel');
INSERT INTO APP_USER(user_id, email, name, password, username) VALUES (7, 'fourthwheel@gmail.com', 'Fortis', 'hashedpassword4', 'fourth_wheel');
INSERT INTO APP_USER(user_id, email, name, password, username) VALUES (8, 'fifthwheel@gmail.com', 'Fivus', 'hashedpassword5', 'fifth_wheel');

INSERT INTO APP_USER_FRIENDS(app_user_user_id, friends_user_id) VALUES (6, 7);
INSERT INTO APP_USER_FRIENDS(app_user_user_id, friends_user_id) VALUES (7, 6);

INSERT INTO EVENT(id, title, all_day, start, end, owner_user_id) VALUES (1, 'Third Event', false, '2023-02-03T12:00:00', '2023-02-04T23:00:00', 6);
INSERT INTO EVENT(id, title, all_day, start, end, owner_user_id) VALUES (3, 'Second Event', false, '2023-02-03T12:00:00', '2023-02-04T23:00:00', 6);
INSERT INTO EVENT(id, title, all_day, start, end, owner_user_id) VALUES (4, 'First Event', false, '2023-02-03T12:00:00', '2023-02-04T23:00:00', 6);
INSERT INTO EVENT(id, title, all_day, start, end, owner_user_id) VALUES (2, 'Fifth Event', true, '2025-02-03T12:00:00', '2025-02-04T00:00:00', 8);
INSERT INTO EVENT_INVITED(event_id, invited_user_id) VALUES(2, 6)
