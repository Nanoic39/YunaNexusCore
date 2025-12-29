package cc.nanoic.yuna.user.mapper;

import cc.nanoic.yuna.user.model.dto.UserDetailDTO;
import cc.nanoic.yuna.user.model.dto.UserDetailDTO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import cc.nanoic.yuna.user.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT u.*, " +
            "ui.user_id as ui_user_id, ui.nickname, ui.avatar_id, ui.gender, ui.birthday, ui.biography, ui.experience, ui.update_time as ui_update_time " +
            "FROM user u " +
            "LEFT JOIN user_info ui ON u.id = ui.user_id " +
            "WHERE u.email = #{email}")
    @Results({
            @Result(column = "ui_user_id", property = "userInfo.user_id"),
            @Result(column = "nickname", property = "userInfo.nickname"),
            @Result(column = "avatar_id", property = "userInfo.avatar_id"),
            @Result(column = "gender", property = "userInfo.gender"),
            @Result(column = "birthday", property = "userInfo.birthday"),
            @Result(column = "biography", property = "userInfo.biography"),
            @Result(column = "experience", property = "userInfo.experience"),
            @Result(column = "ui_update_time", property = "userInfo.updateTime")
    })
    UserDetailDTO selectUserDetailByEmail(@Param("email") String email);

    @Select("SELECT u.*, " +
            "ui.user_id as ui_user_id, ui.nickname, ui.avatar_id, ui.gender, ui.birthday, ui.biography, ui.experience, ui.update_time as ui_update_time " +
            "FROM user u " +
            "LEFT JOIN user_info ui ON u.id = ui.user_id " +
            "WHERE u.username = #{username}")
    @Results({
            @Result(column = "ui_user_id", property = "userInfo.user_id"),
            @Result(column = "nickname", property = "userInfo.nickname"),
            @Result(column = "avatar_id", property = "userInfo.avatar_id"),
            @Result(column = "gender", property = "userInfo.gender"),
            @Result(column = "birthday", property = "userInfo.birthday"),
            @Result(column = "biography", property = "userInfo.biography"),
            @Result(column = "experience", property = "userInfo.experience"),
            @Result(column = "ui_update_time", property = "userInfo.updateTime")
    })
    UserDetailDTO selectUserDetailByUsername(@Param("username") String username);
}