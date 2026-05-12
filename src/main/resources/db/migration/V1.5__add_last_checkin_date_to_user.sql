-- Version: 1.5
-- Description: Ensure last_checkin_date exists in user table
-- Author: Antigravity
-- Date: 2026-05-12

DROP PROCEDURE IF EXISTS AddUserCheckinColumn;

DELIMITER //

CREATE PROCEDURE AddUserCheckinColumn()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_NAME = 'user' 
        AND COLUMN_NAME = 'last_checkin_date' 
        AND TABLE_SCHEMA = DATABASE()
    ) THEN
        ALTER TABLE `user` ADD COLUMN last_checkin_date DATE DEFAULT NULL COMMENT '最后签到日期';
    END IF;
END //

DELIMITER ;

CALL AddUserCheckinColumn();

DROP PROCEDURE IF EXISTS AddUserCheckinColumn;
