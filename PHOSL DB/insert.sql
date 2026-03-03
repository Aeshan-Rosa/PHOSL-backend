USE piano_management;

-- =====================================================
-- 1. USER ROLES
-- =====================================================

INSERT INTO user_roles (role_name) VALUES
('ADMIN'),
('SALES'),
('ACCOUNTANT'),
('TECHNICIAN');

-- =====================================================
-- USERS (password = hashed example)
-- =====================================================

INSERT INTO users (username, email, password_hash, role_id) VALUES
('admin', 'admin@pianohouse.lk', '$2a$10$mockadminhashxxxxxxxxxxxxxxxxxxxxxxxxxxxx', 1),
('sales1', 'sales@pianohouse.lk', '$2a$10$mocksaleshashxxxxxxxxxxxxxxxxxxxxxxxxxxxx', 2);

-- =====================================================
-- LOCATIONS
-- =====================================================

INSERT INTO locations (name, address) VALUES
('Main Showroom', 'Colombo 07'),
('Warehouse', 'Negombo Industrial Zone');

-- =====================================================
-- WORKER ROLES
-- =====================================================

INSERT INTO worker_roles (role_name) VALUES
('Sales Executive'),
('Technician'),
('Accountant');

-- =====================================================
-- WORKERS
-- =====================================================

INSERT INTO workers 
(employee_code, full_name, phone, email, role_id, base_salary, commission_rate, joined_date)
VALUES
('EMP001', 'Nimal Perera', '0771234567', 'nimal@pianohouse.lk', 1, 75000.00, 5.00, '2023-01-10'),
('EMP002', 'Kasun Silva', '0779876543', 'kasun@pianohouse.lk', 2, 65000.00, 0.00, '2022-05-20');

-- =====================================================
-- SUPPLIERS
-- =====================================================

INSERT INTO suppliers (name, phone, email, address, country, reliability_rating) VALUES
('Yamaha Japan Co.', '+81-123-4567', 'sales@yamaha.jp', 'Tokyo, Japan', 'Japan', 5),
('Kawai Distributors', '+94-11-2233445', 'info@kawai.lk', 'Colombo 03', 'Sri Lanka', 4);

-- =====================================================
-- CUSTOMERS
-- =====================================================

INSERT INTO customers (full_name, phone, email, address, how_found) VALUES
('Tharindu Fernando', '0711111111', 'tharindu@gmail.com', 'Gampaha', 'Referral'),
('Dilani Wickramasinghe', '0722222222', 'dilani@gmail.com', 'Colombo 05', 'SocialMedia');

-- =====================================================
-- RECOMMENDERS
-- =====================================================

INSERT INTO recommenders (name, phone, email) VALUES
('Music Teacher - Ms. Shalini', '0775555555', 'shalini@gmail.com');

-- =====================================================
-- PIANOS
-- =====================================================

INSERT INTO pianos 
(piano_code, brand, model, serial_number, piano_type, color, condition_type, manufacture_year, status, location_id, expected_selling_price)
VALUES
('PNO001', 'Yamaha', 'U3', 'YAM123456', 'Upright', 'Black', 'Used', 2015, 'IN_STOCK', 1, 1850000.00),
('PNO002', 'Kawai', 'K300', 'KAW654321', 'Upright', 'Brown', 'Used', 2018, 'IN_STOCK', 1, 2100000.00);

-- =====================================================
-- PURCHASES
-- =====================================================

INSERT INTO purchases (purchase_code, supplier_id, purchase_date, payment_status)
VALUES
('PUR001', 1, '2024-01-15', 'PAID');

INSERT INTO purchase_items 
(purchase_id, piano_id, buy_price, shipping_cost, repair_cost, other_cost, landed_cost)
VALUES
(1, 1, 1200000.00, 150000.00, 50000.00, 10000.00, 1410000.00);

INSERT INTO purchase_payments (purchase_id, pay_date, amount, method)
VALUES
(1, '2024-01-20', 1410000.00, 'BankTransfer');

-- =====================================================
-- SALES
-- =====================================================

INSERT INTO sales 
(invoice_no, customer_id, sold_date, salesperson_id, recommender_id, subtotal, discount, total, payment_plan, warranty_months, delivery_date)
VALUES
('INV001', 1, '2024-02-10', 1, 1, 1850000.00, 50000.00, 1800000.00, 'INSTALLMENT', 12, '2024-02-12');

INSERT INTO sale_items
(sale_id, piano_id, unit_price, line_discount, line_total)
VALUES
(1, 1, 1850000.00, 50000.00, 1800000.00);

-- Update piano status after sale
UPDATE pianos SET status = 'SOLD' WHERE id = 1;

-- =====================================================
-- SALE PAYMENTS
-- =====================================================

INSERT INTO sale_payments (sale_id, pay_date, amount, method)
VALUES
(1, '2024-02-10', 500000.00, 'Cash');

-- =====================================================
-- INSTALLMENT PLAN
-- =====================================================

INSERT INTO installment_plans
(sale_id, total_amount, down_payment, remaining_balance, months, start_date)
VALUES
(1, 1800000.00, 500000.00, 1300000.00, 6, '2024-03-01');

INSERT INTO installment_schedule
(plan_id, installment_no, due_date, amount)
VALUES
(1, 1, '2024-03-01', 216666.67),
(1, 2, '2024-04-01', 216666.67),
(1, 3, '2024-05-01', 216666.67),
(1, 4, '2024-06-01', 216666.67),
(1, 5, '2024-07-01', 216666.67),
(1, 6, '2024-08-01', 216666.65);

-- =====================================================
-- COMMISSIONS
-- =====================================================

INSERT INTO worker_commissions
(sale_id, worker_id, commission_rate, commission_amount)
VALUES
(1, 1, 5.00, 90000.00);

INSERT INTO recommender_commissions
(sale_id, recommender_id, commission_rate, commission_amount)
VALUES
(1, 1, 2.00, 36000.00);

-- =====================================================
-- REPAIRS
-- =====================================================

INSERT INTO piano_repairs
(piano_id, opened_date, issue, repair_cost, technician_id)
VALUES
(2, '2024-01-25', 'Minor tuning and key adjustment', 15000.00, 2);

-- =====================================================
-- TASKS
-- =====================================================

INSERT INTO work_tasks
(task_type, piano_id, customer_id, assigned_worker_id, due_date)
VALUES
('DELIVERY', 1, 1, 2, '2024-02-12'),
('FOLLOWUP', NULL, 1, 1, '2025-02-10');