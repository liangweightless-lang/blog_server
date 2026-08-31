package com.wtls.blog_server.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wtls.blog_server.entity.user.CreatorApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CreatorApplicationMapper extends BaseMapper<CreatorApplication> {

    @Select("SELECT ca.*, u.username, u.nickname as user_nickname, u.avatar_url as user_avatar " +
            "FROM creator_application ca " +
            "LEFT JOIN user u ON ca.user_id = u.id " +
            "ORDER BY ca.id DESC")
    List<Map<String, Object>> selectAllWithUserInfo();

    @Select("SELECT * FROM creator_application WHERE user_id = #{userId} ORDER BY id DESC LIMIT 1")
    CreatorApplication selectLatestByUserId(Long userId);
}
