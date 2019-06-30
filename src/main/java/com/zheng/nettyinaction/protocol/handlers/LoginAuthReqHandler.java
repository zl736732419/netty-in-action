package com.zheng.nettyinaction.protocol.handlers;

import com.zheng.nettyinaction.protocol.bean.NHeader;
import com.zheng.nettyinaction.protocol.bean.NMessage;
import com.zheng.nettyinaction.protocol.enums.EnumMessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;

/**
 * 安全认证，这里实现客户端握手请求
 * @Author zhenglian
 * @Date 2019/6/30
 */
public class LoginAuthReqHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 客户端连接建立时发起握手请求
        NMessage loginReq = buildLoginReq(ctx);
        ctx.writeAndFlush(loginReq);
    }

    private NMessage buildLoginReq(ChannelHandlerContext ctx) {
        NMessage message = new NMessage();
        NHeader header = new NHeader();
        header.setType(EnumMessageType.LOGIN_REQ.value());
        message.setHeader(header);
        // 用户ip
        InetSocketAddress address = (InetSocketAddress) ctx.channel().localAddress();
        String hostname = address.getAddress().getHostAddress();
        System.out.println("client hostname: " + hostname);
        message.setBody(hostname);
        return message;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NMessage message = (NMessage) msg;
        if (null == message || null == message.getHeader()) {
            ctx.fireChannelRead(msg);
            return;
        }

        // 只负责处理握手应答消息
        NHeader header = message.getHeader();
        if (header.getType() != EnumMessageType.LOGIN_RESP.value()) {
            ctx.fireChannelRead(msg);
            return;
        }

        byte loginResult = (byte) message.getBody();
        // 握手失败
        if (loginResult == -1) {
            System.out.println("Login is error.");
            ctx.close();
            return;
        }
        
        System.out.println("Login is OK: " + message);
        ctx.fireChannelRead(msg);
    }
}
