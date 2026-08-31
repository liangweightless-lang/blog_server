package com.wtls.blog_server;

import com.wtls.blog_server.entity.user.User;
import com.wtls.blog_server.mapper.user.UserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BlogServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlogServerApplication.class, args);
	}

	/**
	 * 系统启动自检：若无 admin 管理员账号，自动初始化默认管理员
	 */
	@Bean
	public CommandLineRunner initDefaultAdmin(UserMapper userMapper) {
		return args -> {
			try {
				User admin = userMapper.findByUsername("admin");
				if (admin == null) {
					admin = new User();
					admin.setUsername("admin");
					admin.setPassword("admin");
					admin.setNickname("小柴包主理人");
					admin.setAvatarUrl("/img/admin_avatar.png");
					admin.setPoints(9999);
					admin.setInviteCode("ADMIN888");
					admin.setRole("ADMIN");
					userMapper.insert(admin);
					System.out.println(">>> [系统自动初始化] 默认管理员账号已创建: admin / admin (角色: ADMIN)");
				} else {
					admin.setPassword("admin");
					admin.setRole("ADMIN");
					userMapper.updateById(admin);
					System.out.println(">>> [系统自动同步] 管理员密码已同步重置为: admin / admin");
				}
			} catch (Exception e) {
				// 数据库尚未就绪时忽略，由后续迁移/连接处理
			}
		};
	}
}
