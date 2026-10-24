-- ========================================================
-- Enterprise Employee Portal - Relational Database Schema
-- Compatible with MySQL 8.0+, MariaDB, and SQLite
-- ========================================================

CREATE TABLE IF NOT EXISTS employees (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id VARCHAR(20) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    department VARCHAR(80) NOT NULL,
    designation VARCHAR(80) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'EMPLOYEE',
    leave_balance INT NOT NULL DEFAULT 18,
    join_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS leaves (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id VARCHAR(20) NOT NULL,
    leave_type VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    days INT NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_by VARCHAR(20) DEFAULT NULL,
    remarks TEXT DEFAULT NULL,
    CONSTRAINT fk_leave_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE
);

-- Seed Synthetic Demonstration Data
INSERT INTO employees (employee_id, username, password_hash, salt, full_name, email, department, designation, role, leave_balance, join_date)
VALUES 
('HCL10482', 'sathya.narayan', '47f29a00839e3124508ecbf7ff0086c8f93be9f4df9aeefc97561be9ad55ad9c54e0ad52f82fc461d334522904c600fef0db87b354228c2ff9a706ec9f9b5c39', 'hcltech_salt_sathya_2026', 'Sathya Narayan', 'sathya.narayan@hcltech-demo.com', 'Quality Engineering & Testing', 'Associate Test Engineer', 'EMPLOYEE', 18, '2024-06-15'),
('HCL10051', 'rajesh.kumar', 'c5457ef6722d3d3ea67773f2fa15c3249fae61cb04a5bf4e59174668ba45cf450516631fc3c22b9609f9f9d2557bb5230ffea89f36e8b4e470129bc623dbe66f', 'hcltech_salt_rajesh_2026', 'Rajesh Kumar', 'rajesh.kumar@hcltech-demo.com', 'Quality Engineering & Testing', 'QA Lead / Test Manager', 'MANAGER', 22, '2020-03-01')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

INSERT INTO leaves (employee_id, leave_type, start_date, end_date, days, reason, status, applied_at, reviewed_by, remarks)
VALUES
('HCL10482', 'CASUAL', '2026-11-10', '2026-11-12', 3, 'Family function in hometown', 'APPROVED', '2026-10-20 10:30:00', 'HCL10051', 'Approved. Enjoy the holiday.'),
('HCL10482', 'SICK', '2026-10-28', '2026-10-29', 2, 'Routine medical checkup', 'PENDING', '2026-10-24 08:00:00', NULL, NULL);
