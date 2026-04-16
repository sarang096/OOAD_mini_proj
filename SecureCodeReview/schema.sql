CREATE DATABASE IF NOT EXISTS code_review_db;

USE code_review_db;

CREATE TABLE users (
    userId INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL
);

CREATE TABLE change_requests (
    requestId INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'Submitted',
    createdDate DATE NOT NULL,
    developerId INT,
    FOREIGN KEY (developerId) REFERENCES users(userId)
);

CREATE TABLE reviews (
    reviewId INT PRIMARY KEY AUTO_INCREMENT,
    comments TEXT,
    decision VARCHAR(20),
    requestId INT,
    reviewerId INT,
    FOREIGN KEY (requestId) REFERENCES change_requests(requestId),
    FOREIGN KEY (reviewerId) REFERENCES users(userId)
);

CREATE TABLE approval_rules (
    ruleId INT PRIMARY KEY AUTO_INCREMENT,
    ruleDescription VARCHAR(500) NOT NULL,
    adminId INT,
    FOREIGN KEY (adminId) REFERENCES users(userId)
);

INSERT INTO users (name, email, password, role)
VALUES ('Admin User', 'admin@system.com', 'admin123', 'Admin');

USE code_review_db;
SHOW TABLES;

UPDATE users 
SET role = 'Administrator' 
WHERE email = 'admin@system.com';
USE code_review_db;
SELECT * FROM users;

USE code_review_db;
DESCRIBE users;

SELECT * FROM users;

USE code_review_db;


INSERT INTO users (name, email, password, role) 
VALUES ('Test Admin', 'test@admin.com', 'test123', 'Administrator');

USE code_review_db;
SELECT * FROM users;
SHOW TABLES;
select * from approval_rules;
select * from reviews;
select * from change_requests;