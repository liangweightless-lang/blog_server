CREATE TABLE IF NOT EXISTS `creator_application` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '申请人用户ID',
  `brand_name` varchar(100) NOT NULL COMMENT '主理人/品牌名称',
  `contact_phone` varchar(20) NOT NULL COMMENT '联系手机',
  `wechat_id` varchar(50) DEFAULT NULL COMMENT '联系微信号',
  `intro` text NOT NULL COMMENT '主理人介绍/主营品类',
  `credentials_url` varchar(500) DEFAULT NULL COMMENT '资质/作品展示图',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '审核状态: 0-待审核, 1-已通过, 2-已驳回',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '驳回原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='主理人入驻申请表';
