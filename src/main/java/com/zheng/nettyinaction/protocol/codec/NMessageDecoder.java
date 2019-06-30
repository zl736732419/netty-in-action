package com.zheng.nettyinaction.protocol.codec;

import com.zheng.nettyinaction.protocol.bean.NHeader;
import com.zheng.nettyinaction.protocol.bean.NMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.marshalling.DefaultUnmarshallerProvider;
import io.netty.handler.codec.marshalling.UnmarshallerProvider;
import io.netty.util.CharsetUtil;
import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author zhenglian
 * @Date 2019/6/30
 */
public class NMessageDecoder extends LengthFieldBasedFrameDecoder {
    
    private NMarshallingDecoder decoder;

    public NMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
        MarshallerFactory factory = Marshalling.getProvidedMarshallerFactory("serial");
        MarshallingConfiguration config = new MarshallingConfiguration();
        config.setVersion(5);
        UnmarshallerProvider provider = new DefaultUnmarshallerProvider(factory, config);
        decoder = new NMarshallingDecoder(provider);
    }


    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (null == frame) {
            return null;
        }
        NMessage message = new NMessage();
        NHeader header = new NHeader();
        header.setCrcCode(frame.readInt());
        header.setLength(frame.readInt());
        header.setSessionID(frame.readLong());
        header.setType(frame.readByte());
        header.setPriority(frame.readByte());
        
        // attachment
        int size = frame.readInt();
        if (size > 0) {
            Map<String, Object> attachment = new HashMap<>();
            int keySize;
            byte[] keyArray;
            String key;
            for (int i = 0; i < size; i++) {
                keySize = frame.readInt();
                keyArray = new byte[keySize];
                frame.readBytes(keyArray);
                key = new String(keyArray, CharsetUtil.UTF_8);
                attachment.put(key, decoder.decode(ctx, frame));
            }
            header.setAttachment(attachment);
        }
        message.setHeader(header);
        
        // body
        if (frame.readableBytes() > 4) {
            message.setBody(decoder.decode(ctx, frame));
        }
        return message;
    }
}
