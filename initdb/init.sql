CREATE SCHEMA secure;

CREATE TABLE secure.employee
(
    user_name  VARCHAR(50) PRIMARY KEY,
    first_name VARCHAR(50)    NOT NULL,
    last_name  VARCHAR(50)    NOT NULL,
    salary     NUMERIC(10, 0) NOT NULL,
    manager_id VARCHAR(50),
    FOREIGN KEY (manager_id) REFERENCES secure.employee (user_name)
);

CREATE TABLE secure.account
(
    id       VARCHAR(50) PRIMARY KEY,
    password VARCHAR(50) NOT NULL
);

INSERT INTO secure.employee
VALUES ('bob', 'Bob', 'Denard', 7000, NULL),
       ('betty', 'Betty', 'Boop', 8000, NULL),
       ('alice', 'Alice', 'Cooper', 5000, 'bob'),
       ('charlie', 'Charlie', 'Sheen', 6000, 'betty');

INSERT INTO secure.account
VALUES ('bob', 'bob'),
       ('betty', 'betty'),
       ('alice', 'alice'),
       ('charlie', 'charlie');