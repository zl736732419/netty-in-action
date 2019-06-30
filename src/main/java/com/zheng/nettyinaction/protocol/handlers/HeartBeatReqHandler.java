package com.zheng.nettyinaction.protocol.handlers;

import com.zheng.nettyinaction.protocol.bean.NHeader;
import com.zheng.nettyinaction.protocol.bean.NMessage;
import com.zheng.nettyinaction.protocol.enums.EnumMessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 连接建立成功后，客户端主动发送心跳请求消息
 *
 * @Author zhenglian
 * @Date 2019/6/30
 */
public class HeartBeatReqHandler extends ChannelInboundHandlerAdapter {

    private ScheduledFuture<?> heartBeat;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NMessage message = (NMessage) msg;
        if (null == message || null == message.getHeader()) {
            ctx.fireChannelRead(msg);
            return;
        }

        NHeader header = message.getHeader();
        if (header.getType() == EnumMessageType.LOGIN_RESP.value()) {
            // 发送心跳消息
            heartBeat = ctx.executor().scheduleAtFixedRate(new HeartBeatTask(ctx),
                    0, 5, TimeUnit.SECONDS);
        } else if(header.getType() == EnumMessageType.HEARTBEAT_RESP.value()) {
            // 心跳响应消息
            System.out.println("client receive server heart beat response msg: ---------> " + msg);
        } else {
            // 其他消息不做处理
            ctx.fireChannelRead(msg);
        }
    }

    private static class HeartBeatTask implements Runnable {

        private ChannelHandlerContext ctx;

        public HeartBeatTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            NMessage heartBeatMsg = buildHeartBeatMsg();
            System.out.println("client send heart beat msg: " + heartBeatMsg);
            ctx.writeAndFlush(heartBeatMsg);
        }

        private NMessage buildHeartBeatMsg() {
            NMessage message = new NMessage();
            NHeader header = new NHeader();
            header.setType(EnumMessageType.HEARTBEAT_REQ.value());
            message.setHeader(header);
            return message;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 资源释放
        if (null != heartBeat) {
            heartBeat.cancel(true);
            heartBeat = null;
        }
        ctx.fireExceptionCaught(cause);
    }
}
