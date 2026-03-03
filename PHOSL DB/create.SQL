-- =====================================================
-- CREATE DATABASE
-- =====================================================

CREATE DATABASE piano_management;
USE piano_management;

-- =====================================================
-- 1. USER LOGIN & ROLE SYSTEM
-- =====================================================

CREATE TABLE user_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(80) NOT NULL UNIQUE,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES user_roles(id)
);

-- =====================================================
-- 2. LOCATIONS
-- =====================================================

CREATE TABLE locations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    address VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE
);

-- =====================================================
-- 3. SUPPLIERS
-- =====================================================

CREATE TABLE suppliers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    phone VARCHAR(30),
    email VARCHAR(120),
    address VARCHAR(255),
    country VARCHAR(80),
    reliability_rating INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 4. CUSTOMERS
-- =====================================================

CREATE TABLE customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    email VARCHAR(120),
    address VARCHAR(255),
    how_found ENUM('Referral','SocialMedia','WalkIn','Website','Other') DEFAULT 'Other',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 5. RECOMMENDERS
-- =====================================================

CREATE TABLE recommenders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    phone VARCHAR(30),
    email VARCHAR(120),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 6. WORKER MANAGEMENT
-- =====================================================

CREATE TABLE worker_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE workers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_code VARCHAR(30) UNIQUE,
    full_name VARCHAR(120) NOT NULL,
    phone VARCHAR(30),
    email VARCHAR(120),
    role_id BIGINT NOT NULL,
    base_salary DECIMAL(12,2) DEFAULT 0.00,
    commission_rate DECIMAL(5,2) DEFAULT 0.00,
    joined_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (role_id) REFERENCES worker_roles(id)
);

CREATE TABLE worker_payouts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    worker_id BIGINT NOT NULL,
    period_year INT NOT NULL,
    period_month INT NOT NULL,
    payout_type ENUM('Salary','Commission','Bonus','Deduction') NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payout_date DATE NOT NULL,
    FOREIGN KEY (worker_id) REFERENCES workers(id)
);

-- =====================================================
-- 7. PIANO INVENTORY
-- =====================================================

CREATE TABLE pianos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    piano_code VARCHAR(40) UNIQUE,
    brand VARCHAR(80) NOT NULL,
    model VARCHAR(120),
    serial_number VARCHAR(120) UNIQUE,
    piano_type ENUM('Upright','Grand','Digital','Hybrid','Other') DEFAULT 'Upright',
    color VARCHAR(50),
    condition_type ENUM('New','Used','Refurbished') DEFAULT 'Used',
    manufacture_year INT,
    status ENUM('IN_STOCK','RESERVED','SOLD','UNDER_REPAIR') DEFAULT 'IN_STOCK',
    location_id BIGINT,
    expected_selling_price DECIMAL(12,2),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (location_id) REFERENCES locations(id)
);

-- =====================================================
-- 8. PURCHASES (BUYING HISTORY)
-- =====================================================

CREATE TABLE purchases (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    purchase_code VARCHAR(40) UNIQUE,
    supplier_id BIGINT NOT NULL,
    purchase_date DATE NOT NULL,
    payment_status ENUM('PAID','PARTIAL','PENDING') DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

CREATE TABLE purchase_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    purchase_id BIGINT NOT NULL,
    piano_id BIGINT NOT NULL UNIQUE,
    buy_price DECIMAL(12,2) DEFAULT 0.00,
    shipping_cost DECIMAL(12,2) DEFAULT 0.00,
    repair_cost DECIMAL(12,2) DEFAULT 0.00,
    other_cost DECIMAL(12,2) DEFAULT 0.00,
    landed_cost DECIMAL(12,2) DEFAULT 0.00,
    FOREIGN KEY (purchase_id) REFERENCES purchases(id),
    FOREIGN KEY (piano_id) REFERENCES pianos(id)
);

CREATE TABLE purchase_payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    purchase_id BIGINT NOT NULL,
    pay_date DATE NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    method ENUM('Cash','BankTransfer','Card','Cheque','Other') DEFAULT 'BankTransfer',
    FOREIGN KEY (purchase_id) REFERENCES purchases(id)
);

-- =====================================================
-- 9. SALES
-- =====================================================

CREATE TABLE sales (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    invoice_no VARCHAR(40) UNIQUE,
    customer_id BIGINT NOT NULL,
    sold_date DATE NOT NULL,
    salesperson_id BIGINT,
    recommender_id BIGINT,
    subtotal DECIMAL(12,2) DEFAULT 0.00,
    discount DECIMAL(12,2) DEFAULT 0.00,
    total DECIMAL(12,2) DEFAULT 0.00,
    payment_plan ENUM('FULL','INSTALLMENT') DEFAULT 'FULL',
    warranty_months INT DEFAULT 0,
    delivery_date DATE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (salesperson_id) REFERENCES workers(id),
    FOREIGN KEY (recommender_id) REFERENCES recommenders(id)
);

CREATE TABLE sale_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sale_id BIGINT NOT NULL,
    piano_id BIGINT NOT NULL UNIQUE,
    unit_price DECIMAL(12,2) DEFAULT 0.00,
    line_discount DECIMAL(12,2) DEFAULT 0.00,
    line_total DECIMAL(12,2) DEFAULT 0.00,
    FOREIGN KEY (sale_id) REFERENCES sales(id),
    FOREIGN KEY (piano_id) REFERENCES pianos(id)
);

CREATE TABLE sale_payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sale_id BIGINT NOT NULL,
    pay_date DATE NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    method ENUM('Cash','BankTransfer','Card','Cheque','Other') DEFAULT 'Cash',
    FOREIGN KEY (sale_id) REFERENCES sales(id)
);

-- =====================================================
-- 10. INSTALLMENTS
-- =====================================================

CREATE TABLE installment_plans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sale_id BIGINT UNIQUE,
    total_amount DECIMAL(12,2) NOT NULL,
    down_payment DECIMAL(12,2) DEFAULT 0.00,
    remaining_balance DECIMAL(12,2) DEFAULT 0.00,
    months INT NOT NULL,
    start_date DATE NOT NULL,
    due_day_of_month INT DEFAULT 1,
    status ENUM('ACTIVE','COMPLETED','CANCELLED') DEFAULT 'ACTIVE',
    FOREIGN KEY (sale_id) REFERENCES sales(id)
);

CREATE TABLE installment_schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NOT NULL,
    installment_no INT NOT NULL,
    due_date DATE NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    paid_amount DECIMAL(12,2) DEFAULT 0.00,
    status ENUM('PENDING','PARTIAL','PAID','OVERDUE') DEFAULT 'PENDING',
    FOREIGN KEY (plan_id) REFERENCES installment_plans(id)
);

-- =====================================================
-- 11. COMMISSIONS
-- =====================================================

CREATE TABLE worker_commissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sale_id BIGINT UNIQUE,
    worker_id BIGINT NOT NULL,
    commission_rate DECIMAL(5,2),
    commission_amount DECIMAL(12,2),
    status ENUM('PENDING','PAID') DEFAULT 'PENDING',
    FOREIGN KEY (sale_id) REFERENCES sales(id),
    FOREIGN KEY (worker_id) REFERENCES workers(id)
);

CREATE TABLE recommender_commissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sale_id BIGINT UNIQUE,
    recommender_id BIGINT NOT NULL,
    commission_rate DECIMAL(5,2),
    commission_amount DECIMAL(12,2),
    status ENUM('PENDING','PAID') DEFAULT 'PENDING',
    FOREIGN KEY (sale_id) REFERENCES sales(id),
    FOREIGN KEY (recommender_id) REFERENCES recommenders(id)
);

-- =====================================================
-- 12. REPAIRS & TASKS
-- =====================================================

CREATE TABLE piano_repairs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    piano_id BIGINT NOT NULL,
    opened_date DATE NOT NULL,
    closed_date DATE,
    issue VARCHAR(255),
    repair_cost DECIMAL(12,2),
    technician_id BIGINT,
    status ENUM('OPEN','IN_PROGRESS','DONE') DEFAULT 'OPEN',
    FOREIGN KEY (piano_id) REFERENCES pianos(id),
    FOREIGN KEY (technician_id) REFERENCES workers(id)
);

CREATE TABLE work_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_type ENUM('DELIVERY','TUNING','REPAIR','FOLLOWUP','OTHER') DEFAULT 'OTHER',
    piano_id BIGINT,
    customer_id BIGINT,
    assigned_worker_id BIGINT,
    due_date DATE,
    status ENUM('TODO','IN_PROGRESS','DONE','CANCELLED') DEFAULT 'TODO',
    FOREIGN KEY (piano_id) REFERENCES pianos(id),
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (assigned_worker_id) REFERENCES workers(id)
);