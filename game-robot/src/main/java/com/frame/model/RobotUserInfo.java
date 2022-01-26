package com.frame.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RobotUserInfo {
    /**
     * 玩家id
     */
    private long userId; 
    /**
     * 名字
     */
    private String name; 
    /**
     * 金币额
     */
    private long balance; 
    /**
     * 头像
     */
    private String headPic; 
    /**
     * 头像类型 0原始头像 1道具头像
     */
    private int headPicType; 
    /**
     * 等级
     */
   private int level;
}
