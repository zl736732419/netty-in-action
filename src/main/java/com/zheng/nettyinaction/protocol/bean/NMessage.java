package com.zheng.nettyinaction.protocol.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @Author zhenglian
 * @Date 2019/6/30
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class NMessage {
    /**
     * 消息头
     */
    private NHeader header;
    /**
     * 消息体
     */
    private Object body;
}
