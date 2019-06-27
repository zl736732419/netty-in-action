package com.zheng.nettyinaction.timeserver.server.codec.msgpack;

import com.zheng.nettyinaction.codecstudy.Person;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.msgpack.MessagePack;

import java.util.List;

/**
 * 基于message pack解码器
 * @Author zhenglian
 * @Date 2019/6/27
 */
public class MessagePackDecoder extends ByteToMessageDecoder {
    private MessagePack messagePack = new MessagePack();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        Person person = messagePack.read(bytes, Person.class);
        out.add(person);
    }
}
