package com.zheng.nettyinaction.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.marshalling.MarshallingDecoder;
import io.netty.handler.codec.marshalling.UnmarshallerProvider;

/**
 * @Author zhenglian
 * @Date 2019/6/30
 */
public class NMarshallingDecoder extends MarshallingDecoder {
    
    public NMarshallingDecoder(UnmarshallerProvider provider) {
        super(provider);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        return super.decode(ctx, in);
    }
}
