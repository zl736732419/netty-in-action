package com.zheng.nettyinaction.protocol.enums;

public enum EnumMessageType {

    /**
     * 业务请求消息
     */
    SERVICE_REQ((byte) 0),
    /**
     * 业务响应消息
     */
    SERVICE_RESP((byte) 1),
    /**
     * 业务请求响应消息
     */
    ONE_WAY((byte) 2),
    /**
     * 握手请求消息
     */
    LOGIN_REQ((byte) 3),
    /**
     * 握手响应消息
     */
    LOGIN_RESP((byte) 4),
    /**
     * 心跳请求消息
     */
    HEARTBEAT_REQ((byte) 5),
    /**
     * 心跳响应消息
     */
    HEARTBEAT_RESP((byte) 6);

    private byte value;

    EnumMessageType(byte value) {
        this.value = value;
    }

    public byte value() {
        return this.value;
    }
}