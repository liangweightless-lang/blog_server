package com.wtls.blog_server.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wtls.blog_server.entity.user.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT * FROM user WHERE invite_code = #{inviteCode}")
    User findByInviteCode(String inviteCode);

    @Update("UPDATE user SET points = points + #{pointsToAdd} WHERE id = #{id}")
    void addPoints(@Param("id") Long id, @Param("pointsToAdd") int pointsToAdd);

    @Update("UPDATE user SET points = points - #{pointsToDeduct} WHERE id = #{id} AND points >= #{pointsToDeduct}")
    int deductPoints(@Param("id") Long id, @Param("pointsToDeduct") int pointsToDeduct);

    @Update("UPDATE user SET nickname = #{nickname}, avatar_url = #{avatarUrl}, address = #{address}, " +
            "wechat_id = #{wechatId}, age = #{age}, gender = #{gender} WHERE id = #{id}")
    void updateProfile(User user);

    @Update("UPDATE user SET last_checkin_date = CURRENT_DATE WHERE id = #{id}")
    void updateCheckinDate(Long id);

    @Select("SELECT * FROM user ORDER BY id DESC")
    java.util.List<User> findAll();
}

