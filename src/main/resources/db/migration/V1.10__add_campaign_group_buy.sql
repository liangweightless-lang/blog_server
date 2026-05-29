CREATE TABLE IF NOT EXISTS delivery_location (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL,
    contact_name VARCHAR(100),
    contact_phone VARCHAR(50),
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS group_buy_campaign (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    intro TEXT,
    delivery_location_id BIGINT,
    start_time TIMESTAMP NULL,
    end_time TIMESTAMP NULL,
    delivery_time TIMESTAMP NULL,
    status INT DEFAULT 0, -- 0: pending, 1: active, 2: ended
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS group_buy_campaign_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    group_price DECIMAL(10,2) NOT NULL,
    stock_limit INT DEFAULT -1,
    sort_order INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS campaign_order (
    id VARCHAR(64) PRIMARY KEY, -- string order ID
    campaign_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    follow_number INT NOT NULL, -- 跟团号
    total_amount DECIMAL(10,2) NOT NULL,
    contact_name VARCHAR(100) NOT NULL,
    contact_phone VARCHAR(50) NOT NULL,
    status INT DEFAULT 0, -- 0: unpaid, 1: paid, 2: picked_up, 3: refunded
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS campaign_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(64) NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_image VARCHAR(500),
    specs VARCHAR(500),
    price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
