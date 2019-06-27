package com.zheng.nettyinaction.timeserver.server.codec.msgpack;

import com.zheng.nettyinaction.codecstudy.Person;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.MessagePack;

/**
 * 基于message pack编码器
 * @Author zhenglian
 * @Date 2019/6/27
 */
public class MessagePackEncoder extends MessageToByteEncoder<Person> {
    private MessagePack pack = new MessagePack();
    
    @Override
    protected void encode(ChannelHandlerContext ctx, Person person, ByteBuf out) throws Exception {
        byte[] bytes = pack.write(person);
        out.writeBytes(bytes);
    }
}
