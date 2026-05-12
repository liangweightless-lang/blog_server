-- Version: 1.3
-- Description: Add specs and stock to product table (Idempotent version)
-- Author: Antigravity
-- Date: 2026-05-12

DROP PROCEDURE IF EXISTS AddProductInventoryColumns;

DELIMITER //

CREATE PROCEDURE AddProductInventoryColumns()
BEGIN
    -- Check and add 'stock' column
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_NAME = 'product' 
        AND COLUMN_NAME = 'stock' 
        AND TABLE_SCHEMA = DATABASE()
    ) THEN
        ALTER TABLE product ADD COLUMN stock INT DEFAULT 0 COMMENT '库存数量';
    END IF;

    -- Check and add 'specs' column
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_NAME = 'product' 
        AND COLUMN_NAME = 'specs' 
        AND TABLE_SCHEMA = DATABASE()
    ) THEN
        ALTER TABLE product ADD COLUMN specs TEXT COMMENT '规格定义 (JSON格式)';
    END IF;
END //

DELIMITER ;

CALL AddProductInventoryColumns();

DROP PROCEDURE IF EXISTS AddProductInventoryColumns;
