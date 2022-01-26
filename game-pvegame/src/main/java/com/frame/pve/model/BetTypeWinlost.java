package com.frame.pve.model;

import lombok.Getter;
import lombok.Setter;

/**
 * BetTypeWinlost.java
 * @author Sanjie
 * @date 2021-10-14 15:48
 * @version 1.0.0
 */
@Getter
@Setter
public class BetTypeWinlost {
    /**
     * 下注类型
     */
    private int betType;            
    /**
     * 输赢的筹码
     */
    private long winlost;        
    /**
     * 下注金额
     */
    private long betAmount;     
}
