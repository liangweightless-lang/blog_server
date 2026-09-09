ALTER TABLE `article` ADD COLUMN `is_top` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶(0:否, 1:是)' AFTER `likes_count`;
