-- V1.13: 增加商品状态与配送费，增加订单数量、手机号、备注与配送费字段

-- 1. 商品表扩展
ALTER TABLE `product` 
ADD COLUMN `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态(1:上架销售中, 0:已下架仓库中)' AFTER `is_digital`,
ADD COLUMN `delivery_fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '配送费' AFTER `group_price`;

-- 2. 订单表扩展
ALTER TABLE `product_order`
ADD COLUMN `quantity` INT NOT NULL DEFAULT 1 COMMENT '购买数量' AFTER `product_id`,
ADD COLUMN `contact_phone` VARCHAR(32) NULL COMMENT '联系手机号' AFTER `shipping_address`,
ADD COLUMN `remark` VARCHAR(500) NULL COMMENT '顾客备注' AFTER `contact_phone`,
ADD COLUMN `delivery_fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '配送费' AFTER `points_used`;
