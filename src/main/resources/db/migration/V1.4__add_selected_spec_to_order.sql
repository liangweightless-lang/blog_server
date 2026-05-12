-- Version: 1.4
-- Description: Add selected_spec to product_order table (Idempotent version)
-- Author: Antigravity
-- Date: 2026-05-12

DROP PROCEDURE IF EXISTS AddOrderSpecColumn;

DELIMITER //

CREATE PROCEDURE AddOrderSpecColumn()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_NAME = 'product_order' 
        AND COLUMN_NAME = 'selected_spec' 
        AND TABLE_SCHEMA = DATABASE()
    ) THEN
        ALTER TABLE product_order ADD COLUMN selected_spec VARCHAR(255) COMMENT '选择的商品规格';
    END IF;
END //

DELIMITER ;

CALL AddOrderSpecColumn();

DROP PROCEDURE IF EXISTS AddOrderSpecColumn;
