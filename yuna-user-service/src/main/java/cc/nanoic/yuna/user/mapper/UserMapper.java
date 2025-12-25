package cc.nanoic.yuna.user.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import cc.nanoic.yuna.user.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
