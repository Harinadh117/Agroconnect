CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(10) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('FARMER', 'WORKER', 'KAVULU', 'ADMIN') NOT NULL,
    village VARCHAR(100),
    district VARCHAR(100),
    state VARCHAR(100),
    work_type VARCHAR(100),
    experience VARCHAR(200),
    available_days INT DEFAULT 0,
    available_from DATE,
    preferred_crop VARCHAR(100),
    verified BOOLEAN DEFAULT FALSE,
    suspended BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS jobs (
    job_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    farmer_id BIGINT NOT NULL,
    work_type VARCHAR(100) NOT NULL,
    duration_days INT NOT NULL,
    workers_required INT NOT NULL DEFAULT 1,
    village VARCHAR(100),
    district VARCHAR(100),
    state VARCHAR(100),
    experience_required VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (farmer_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS job_applications (
    application_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    worker_id BIGINT NOT NULL,
    status ENUM('PENDING', 'SELECTED', 'REJECTED') DEFAULT 'PENDING',
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (job_id) REFERENCES jobs(job_id) ON DELETE CASCADE,
    FOREIGN KEY (worker_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS land_listings (
    land_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    farmer_id BIGINT NOT NULL,
    village VARCHAR(100),
    district VARCHAR(100),
    state VARCHAR(100),
    area DECIMAL(10,2) NOT NULL,
    crop_type VARCHAR(100),
    duration VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (farmer_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS land_requests (
    request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    land_id BIGINT NOT NULL,
    kavulu_id BIGINT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (land_id) REFERENCES land_listings(land_id) ON DELETE CASCADE,
    FOREIGN KEY (kavulu_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Seed default admin (password: admin123)
INSERT IGNORE INTO users (name, phone, password, role, village, district, state, verified)
VALUES ('Admin', '0000000000', '$2a$10$2gwV8nhGyG6AQRr2cdYqcOg2lGY5wBe/8c6Y2ECFGO2jqAwx9CYdy', 'ADMIN', 'System', 'System', 'System', TRUE);
