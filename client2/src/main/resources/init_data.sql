CREATE TABLE ACCOUNT (
    account_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255),
    balance INT,
    reserved_balance INT DEFAULT 0,
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


INSERT INTO ACCOUNT (account_id, name, balance) VALUES
  (1, 'user1', 3000),
  (2, 'user2', 50000),
  (3, 'user3', 100);