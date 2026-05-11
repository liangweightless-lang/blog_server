-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS blog_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE blog_db;

-- 2. 创建文章表
CREATE TABLE IF NOT EXISTS `article` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL COMMENT '文章标题',
  `content` longtext NOT NULL COMMENT '文章内容',
  `cover_url` varchar(500) DEFAULT NULL COMMENT '封面图片URL',
  `media_urls` longtext DEFAULT NULL COMMENT '媒体资源(JSON数组)',
  `likes_count` int(11) NOT NULL DEFAULT 0 COMMENT '点赞数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='博客文章表';

-- 3. 创建评论表
CREATE TABLE IF NOT EXISTS `comment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `article_id` bigint(20) NOT NULL COMMENT '关联文章ID',
  `author_name` varchar(50) NOT NULL DEFAULT '热心网友' COMMENT '评论人昵称',
  `content` varchar(500) NOT NULL COMMENT '评论内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章评论表';

-- 4. 创建商品表
CREATE TABLE IF NOT EXISTS `product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT '商品名称',
  `description` varchar(500) NOT NULL COMMENT '商品描述',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `image` varchar(255) NOT NULL COMMENT '封面图片URL',
  `is_digital` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否为数字商品(1:是, 0:否)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小商城商品表';

-- 5. 插入初始商品数据 (由于 IF NOT EXISTS 不会影响现有表，但插入操作需要防重复，所以这里使用 INSERT IGNORE)
INSERT IGNORE INTO `product` (`id`, `name`, `description`, `price`, `image`, `is_digital`) VALUES
(1, '法式经典黄油可颂', '采用法国进口发酵黄油，经过繁复折叠工艺，外层酥脆掉渣，内里柔软蜂巢组织。', 22.00, '/img/product_croissant.png', 1),
(2, '意式特浓提拉米苏', '正宗马斯卡彭芝士搭配浓缩咖啡与少量朗姆酒，口感层次丰富，入口即化。', 38.00, '/img/product_tiramisu.png', 1),
(3, '手冲澳白咖啡 Flat White', '精选埃塞俄比亚 SOE 咖啡豆，完美融合细腻奶泡，带来丝滑浓郁的温暖体验。', 28.00, '/img/product_coffee.png', 0);

-- 6. 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '登录账号',
  `password` varchar(100) NOT NULL COMMENT '登录密码',
  `nickname` varchar(50) NOT NULL COMMENT '用户昵称',
  `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像',
  `points` int(11) NOT NULL DEFAULT 0 COMMENT '积分',
  `invite_code` varchar(20) NOT NULL COMMENT '专属邀请码',
  `invited_by` bigint(20) DEFAULT NULL COMMENT '邀请人的ID',
  `role` varchar(20) NOT NULL DEFAULT 'USER' COMMENT '角色(USER/ADMIN)',
  `address` text COMMENT '地址',
  `wechat_id` varchar(50) COMMENT '微信号',
  `age` int(11) COMMENT '年龄',
  `gender` varchar(20) COMMENT '性别',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_invite_code` (`invite_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 7. 创建订单表
CREATE TABLE IF NOT EXISTS `product_order` (
  `id` varchar(64) NOT NULL COMMENT '订单号(UUID或生成规则)',
  `user_id` bigint(20) NOT NULL COMMENT '购买人ID',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `amount` decimal(10,2) NOT NULL COMMENT '实付金额',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态(0:待支付, 1:已支付, 2:已取消)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品订单表';

-- 8. 创建拼团活动表
CREATE TABLE IF NOT EXISTS `group_buy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `initiator_id` bigint(20) NOT NULL COMMENT '发起人ID',
  `required_num` int(11) NOT NULL DEFAULT 8 COMMENT '成团人数',
  `current_num` int(11) NOT NULL DEFAULT 1 COMMENT '当前人数',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态(0:拼团中, 1:成功, 2:失败/过期)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开启时间',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_initiator_id` (`initiator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼团活动表';

-- 9. 创建拼团成员表
CREATE TABLE IF NOT EXISTS `group_buy_member` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_id` bigint(20) NOT NULL COMMENT '拼团ID',
  `user_id` bigint(20) NOT NULL COMMENT '成员ID',
  `order_id` varchar(64) NOT NULL COMMENT '关联订单号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_user` (`group_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼团成员表';

