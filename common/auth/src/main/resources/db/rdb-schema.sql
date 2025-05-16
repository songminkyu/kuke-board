-- 사용자 테이블 생성
CREATE TABLE users (
                       id BIGINT(20) NOT NULL AUTO_INCREMENT,
                       email VARCHAR(50) NOT NULL,
                       password VARCHAR(120) DEFAULT NULL,
                       username VARCHAR(20) DEFAULT NULL,
                       PRIMARY KEY (id),
                       UNIQUE KEY (email),
                       UNIQUE KEY (username)
);

-- 역할 테이블 생성
CREATE TABLE roles (
                       id INT(11) NOT NULL AUTO_INCREMENT,
                       name VARCHAR(20) DEFAULT NULL,
                       PRIMARY KEY (id)
);

-- 사용자-역할 연결 테이블 생성 (다대다 관계)
CREATE TABLE user_roles (
                            user_id BIGINT(20) NOT NULL,
                            role_id INT(11) NOT NULL,
                            PRIMARY KEY (user_id, role_id),
                            FOREIGN KEY (user_id) REFERENCES users(id),
                            FOREIGN KEY (role_id) REFERENCES roles(id)
);

INSERT INTO roles(name) VALUES('ROLE_USER');
INSERT INTO roles(name) VALUES('ROLE_MODERATOR');
INSERT INTO roles(name) VALUES('ROLE_ADMIN');