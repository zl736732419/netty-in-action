package com.zheng.nettyinaction.codec;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @Author zhenglian
 * @Date 2019/6/27
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

//@Message
public class Person 
        implements Serializable  
{
    public static final long serialVersionUID = 1L;
    
    private Long userId;
    private String name;
    private String master;
}
