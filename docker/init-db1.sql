CREATE TABLE users (
    user_id VARCHAR PRIMARY KEY,
    login VARCHAR,
    first_name VARCHAR,
    last_name VARCHAR
);

INSERT INTO users (user_id, login, first_name, last_name) VALUES
('u1', 'user-1', 'User', 'Userenko'),
('u2', 'user-2', 'Testuser', 'Testov');
