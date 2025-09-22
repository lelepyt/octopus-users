CREATE TABLE user_table (
    ldap_login VARCHAR PRIMARY KEY,
    name VARCHAR,
    surname VARCHAR
);

INSERT INTO user_table (ldap_login, name, surname) VALUES
('ldap1', 'Alice', 'Ivanova'),
('ldap2', 'Bob', 'Petrov');