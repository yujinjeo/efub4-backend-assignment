create table if not exists account (
    account_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(60) NOT NULL,
    password varchar(255) not null,
    nickname varchar(16) not null,
    university varchar(20) not null,
    student_id varchar(255) not null,
    status VARCHAR(255) not null
);

INSERT INTO account (email, password, nickname, university, student_id, status)
VALUES ('yujinalice00@gmail.com', 'yujin00lice!', 'floweralice', 'ewha university', '2062084', 'REGISTERED');

