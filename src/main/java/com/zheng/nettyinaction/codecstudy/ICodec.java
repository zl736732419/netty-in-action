package com.zheng.nettyinaction.codecstudy;

/**
 * @Author zhenglian
 * @Date 2019/6/27
 */
public interface ICodec<T> {
    byte[] encode(T t);
    T decode(byte[] bytes, Class<T> clazz);
}
