package cc.nanoic.yuna.user.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoVO {
    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像(自动拼接URL)
     */
    private String avatar;
    
    /**
     * 性别(0:未知,1:男,2:女 -> 处理为字符串"隐藏","男","女")
     */
    private String gender;

     /**
      * 个人简介
      */
    private String biography;

     /**
      * 经验值
      */
    private Integer experience;

}
