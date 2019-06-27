package com.zheng.nettyinaction.codecstudy.msgpack;

import com.zheng.nettyinaction.codecstudy.ICodec;
import com.zheng.nettyinaction.codecstudy.Person;
import org.msgpack.MessagePack;

import java.io.IOException;

/**
 * 基于message pack编解码
 * quick start document:
 *  * https://github.com/msgpack/msgpack-java/wiki/QuickStart-for-msgpack-java-0.6.x-(obsolete)
 *  
 * @Author zhenglian
 * @Date 2019/6/27
 */
public class MessagePackCodec implements ICodec {
    private MessagePack pack;
   
    public MessagePackCodec() {
        pack = new MessagePack();
        pack.register(Person.class);
    }
    
    @Override
    public <T> byte[] encode(T t) {
        byte[] bytes = null;
        try {
            bytes = pack.write(t);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    @Override
    public <T> T decode(byte[] bytes, Class<T> clazz) {
        T t = null;
        try {
            t = pack.read(bytes, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return t;
    }
}
