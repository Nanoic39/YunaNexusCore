package cc.nanoic.yuna.user.model.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserUpdateDTO {
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 性别
     */
    private Integer gender;
    
    /**
     * 简介
     */
    private String biography;
    
    /**
     * 头像(文件UUID)
     */
    private String avatar;
    
    /**
     * 生日
     */
    private LocalDateTime birthday;
}
