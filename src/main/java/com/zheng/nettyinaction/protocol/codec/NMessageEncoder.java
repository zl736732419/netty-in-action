package com.zheng.nettyinaction.protocol.codec;

import com.zheng.nettyinaction.protocol.bean.NHeader;
import com.zheng.nettyinaction.protocol.bean.NMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.marshalling.DefaultMarshallerProvider;
import io.netty.util.CharsetUtil;
import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;

import java.util.List;
import java.util.Map;

/**
 * 消息解码器
 * @Author zhenglian
 * @Date 2019/6/30
 */
public class NMessageEncoder extends MessageToMessageEncoder<NMessage> {
    
    private NMarshallingEncoder encoder;
    
    public NMessageEncoder() {
        MarshallerFactory factory = Marshalling.getProvidedMarshallerFactory("serial");
        MarshallingConfiguration config = new MarshallingConfiguration();
        config.setVersion(5);
        encoder = new NMarshallingEncoder(new DefaultMarshallerProvider(factory, config));
    }
    
    @Override
    protected void encode(ChannelHandlerContext ctx, NMessage msg, List<Object> out) throws Exception {
        if (null == msg || null == msg.getHeader()) {
            throw new Exception("the encode message is null");
        }
        NHeader header = msg.getHeader();
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(header.getCrcCode());
        buffer.writeInt(header.getLength());
        buffer.writeLong(header.getSessionID());
        buffer.writeByte(header.getType());
        buffer.writeByte(header.getPriority());
        
        // attachment
        buffer.writeInt(header.getAttachment().size());
        byte[] keyArray;
        for (Map.Entry<String, Object> attachment : header.getAttachment().entrySet()) {
            keyArray = attachment.getKey().getBytes(CharsetUtil.UTF_8);
            buffer.writeInt(keyArray.length);
            buffer.writeBytes(keyArray);
            encoder.encode(ctx, attachment.getValue(), buffer);
        }
        
        if (null != msg.getBody()) {
            // marshalling编码时添加了长度字段
            encoder.encode(ctx, msg.getBody(), buffer);
        } else {
            buffer.writeInt(0);
        }
        buffer.setInt(4, buffer.readableBytes());
        out.add(buffer);
    }
}
