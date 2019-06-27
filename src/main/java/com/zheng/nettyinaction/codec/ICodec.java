package com.zheng.nettyinaction.codec;

/**
 * @Author zhenglian
 * @Date 2019/6/27
 */
public interface ICodec {
    <T> byte[] encode(T t);
    <T> T decode(byte[] bytes, Class<T> clazz);
}
