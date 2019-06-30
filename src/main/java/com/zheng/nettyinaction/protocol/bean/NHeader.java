package com.zheng.nettyinaction.protocol.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author zhenglian
 * @Date 2019/6/30
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class NHeader {
    /**
     * netty校验码
     */
    private int crcCode;
    /**
     * 消息长度
     */
    private int length;
    /**
     * 会话ID
     */
    private long sessionID;
    /**
     * 消息类型
     */
    private byte type;
    /**
     * 消息优先级
     */
    private byte priority;
    /**
     * 附件
     */
    private Map<String, Object> attachment = new HashMap<>();
}
