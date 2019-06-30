package com.zheng.nettyinaction.protocol.handlers;

import com.zheng.nettyinaction.protocol.bean.NHeader;
import com.zheng.nettyinaction.protocol.bean.NMessage;
import com.zheng.nettyinaction.protocol.enums.EnumMessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 服务端处理心跳请求消息
 *
 * @Author zhenglian
 * @Date 2019/6/30
 */
public class HeartBeatRespHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NMessage message = (NMessage) msg;
        if (null == message || null == message.getHeader()) {
            ctx.fireChannelRead(msg);
            return;
        }

        NHeader header = message.getHeader();
        if (header.getType() == EnumMessageType.HEARTBEAT_REQ.value()) {
            System.out.println("server receive heart beat msg: ----> " + msg);
            NMessage heartBeatResp = buildHeartBeatResp();
            ctx.writeAndFlush(heartBeatResp);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private NMessage buildHeartBeatResp() {
        NMessage message = new NMessage();
        NHeader header = new NHeader();
        header.setType(EnumMessageType.HEARTBEAT_RESP.value());
        message.setHeader(header);
        return message;
    }
}
