-- Liquibase formatted SQL
-- changeset youssefagagg:v1.2025-06-10T00:00:00 context:local,dev
-- comment: Add dummy users and products with inventory

-- Add 5 dummy users (passwords are 'password')
INSERT INTO users (id, username, password_hash, first_name, last_name, email, phone, created_by, created_date,
                   last_modified_by, last_modified_date, version)
VALUES (1001, 'user1', '$2a$10$1rDWxzZicXW4i1Srm2qcK.MIIS9yf79HtDHkr9Zmuc3qLg1LVGe.O', 'John', 'Doe',
        'john.doe@example.com', '+1234567890', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
       (1002, 'user2', '$2a$10$1rDWxzZicXW4i1Srm2qcK.MIIS9yf79HtDHkr9Zmuc3qLg1LVGe.O', 'Jane', 'Smith',
        'jane.smith@example.com', '+1234567891', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
       (1003, 'user3', '$2a$10$1rDWxzZicXW4i1Srm2qcK.MIIS9yf79HtDHkr9Zmuc3qLg1LVGe.O', 'Bob', 'Johnson',
        'bob.johnson@example.com', '+1234567892', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
       (1004, 'user4', '$2a$10$1rDWxzZicXW4i1Srm2qcK.MIIS9yf79HtDHkr9Zmuc3qLg1LVGe.O', 'Alice', 'Williams',
        'alice.williams@example.com', '+1234567893', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
       (1005, 'user5', '$2a$10$1rDWxzZicXW4i1Srm2qcK.MIIS9yf79HtDHkr9Zmuc3qLg1LVGe.O', 'Charlie', 'Brown',
        'charlie.brown@example.com', '+1234567894', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Assign ROLE_USER to all dummy users
INSERT INTO user_role (user_id, role_name)
VALUES (1001, 'ROLE_USER'),
       (1002, 'ROLE_USER'),
       (1003, 'ROLE_USER'),
       (1004, 'ROLE_USER'),
       (1005, 'ROLE_USER');

-- Add 1000 dummy products with inventory
DO '
DECLARE
    i INT;
    product_id INT;
    product_name VARCHAR(100);
    product_description VARCHAR(1000);
    product_price DECIMAL(10, 2);
    inventory_quantity INT;
BEGIN
FOR i IN 1..1000 LOOP
    -- Generate product data
    product_name := ''Product '' || i;
    product_description := ''Description for product '' || i;
    product_price := (random() * 1000)::DECIMAL(10, 2);
    inventory_quantity := (random() * 100)::INT + 10;

    -- Insert product
    INSERT INTO products (name, description, price, created_by, created_date, last_modified_by, last_modified_date, version)
    VALUES (product_name, product_description, product_price, ''system'', CURRENT_TIMESTAMP, ''system'', CURRENT_TIMESTAMP, 0)
    RETURNING id INTO product_id;

    -- Insert inventory for the product
    INSERT INTO inventory (product_id, quantity, reserved_quantity, created_by, created_date, last_modified_by, last_modified_date, version)
    VALUES (product_id, inventory_quantity, 0, ''system'', CURRENT_TIMESTAMP, ''system'', CURRENT_TIMESTAMP, 0);
    END LOOP;
END
';
