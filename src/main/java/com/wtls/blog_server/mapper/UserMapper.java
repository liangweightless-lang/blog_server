package com.wtls.blog_server.mapper;

import com.wtls.blog_server.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT * FROM user WHERE invite_code = #{inviteCode}")
    User findByInviteCode(String inviteCode);

    @Insert("INSERT INTO user(username, password, nickname, avatar_url, points, invite_code, invited_by, role, create_time) " +
            "VALUES(#{username}, #{password}, #{nickname}, #{avatarUrl}, #{points}, #{inviteCode}, #{invitedBy}, #{role}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    @Update("UPDATE user SET points = points + #{pointsToAdd} WHERE id = #{id}")
    void addPoints(@Param("id") Long id, @Param("pointsToAdd") int pointsToAdd);

    @Update("UPDATE user SET points = points - #{pointsToDeduct} WHERE id = #{id} AND points >= #{pointsToDeduct}")
    int deductPoints(@Param("id") Long id, @Param("pointsToDeduct") int pointsToDeduct);

    @Update("UPDATE user SET nickname = #{nickname}, avatar_url = #{avatarUrl}, address = #{address}, " +
            "wechat_id = #{wechatId}, age = #{age}, gender = #{gender} WHERE id = #{id}")
    void updateProfile(User user);

    @Select("SELECT * FROM user ORDER BY id DESC")
    java.util.List<User> findAll();
}
