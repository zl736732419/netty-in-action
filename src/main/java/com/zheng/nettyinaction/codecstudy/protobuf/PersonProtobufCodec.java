package com.zheng.nettyinaction.codecstudy.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import com.zheng.nettyinaction.codecstudy.ICodec;

/**
 * @Author zhenglian
 * @Date 2019/6/27
 */
public class PersonProtobufCodec implements ICodec {
    @Override
    public <T> byte[] encode(T t) {
        PersonModule.Person person = (PersonModule.Person) t;
        return person.toByteArray();
    }

    @Override
    public <T> T decode(byte[] bytes, Class<T> clazz) {
        T person = null;
        try {
            person = (T) PersonModule.Person.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return person;
    }
}
