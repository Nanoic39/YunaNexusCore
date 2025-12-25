package cc.nanoic.yuna.user.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import cc.nanoic.yuna.user.entity.UserInfo;

@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
}
