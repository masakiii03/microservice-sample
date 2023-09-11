CREATE TABLE PRODUCT (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(255),
    quantity INT,
    reserved_quantity INT DEFAULT 0,
    price INT,
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


INSERT INTO PRODUCT (product_id, product_name, quantity, price) VALUES
  (1, 'cheese', 10, 300),
  (2, 'book1', 3, 1500),
  (3, 'car', 2, 3000000),
  (4, 'book2', 5, 1800);