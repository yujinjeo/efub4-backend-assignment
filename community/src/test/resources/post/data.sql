create table if not exists account (
    account_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(60) NOT NULL,
    password varchar(255) not null,
    nickname varchar(16) not null,
    university varchar(20) not null,
    student_id varchar(255) not null,
    status VARCHAR(255) not null
);

CREATE TABLE if not exists board (
    board_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    board_name VARCHAR(50) NOT NULL,
    board_description VARCHAR(1000) NOT NULL,
    board_notice VARCHAR(1000) NOT NULL,
    FOREIGN KEY (account_id) REFERENCES Account(account_id)
);

create table if not exists post (
    post_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    board_id BIGINT NOT NULL,
    title VARCHAR(50) NOT NULL,
    content VARCHAR(1000) NOT NULL,
    writer_open VARCHAR(10) NOT NULL,
    FOREIGN KEY (account_id) REFERENCES account(account_id),
    FOREIGN KEY (board_id) REFERENCES board(board_id)
);
INSERT INTO account (email, password, nickname, university, student_id, status)
VALUES ('yujinalice00@gmail.com', 'yujinalic00!', 'floweralice', 'ewha university', '2062084', 'REGISTERED');

INSERT INTO account (email, password, nickname, university, student_id, status)
VALUES ('yuj00@gmail.com', 'yujinic00!', 'brushice', 'ewha university', '2062084', 'REGISTERED');

INSERT INTO board (account_id, board_name, board_description, board_notice)
VALUES (1, 'Notice Board', 'This is a board for general notices.', 'Please adhere to the board rules.');

INSERT INTO post (account_id, board_id, title, content, writer_open)
VALUES (1, 1, 'Welcome to the Board', 'This is the first post on this board.', 'false');

