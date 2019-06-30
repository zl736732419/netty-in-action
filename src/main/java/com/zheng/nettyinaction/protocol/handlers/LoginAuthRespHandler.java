package com.zheng.nettyinaction.protocol.handlers;

import com.zheng.nettyinaction.protocol.bean.NHeader;
import com.zheng.nettyinaction.protocol.bean.NMessage;
import com.zheng.nettyinaction.protocol.enums.EnumMessageType;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端基于IP白名单进行安全认证
 * @Author zhenglian
 * @Date 2019/6/30
 */
@ChannelHandler.Sharable
public class LoginAuthRespHandler extends ChannelInboundHandlerAdapter {

    private static Map<String, Boolean> nodeCheck = new ConcurrentHashMap<>();
    private Set<String> whiteList = new HashSet<>();

    public LoginAuthRespHandler() {
        whiteList.add("127.0.0.1");
        whiteList.add("192.168.3.12");
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NMessage message = (NMessage) msg;
        if (null == message || null == message.getHeader()) {
            ctx.fireChannelRead(msg);
            return;
        }

        // 只负责处理握手请求消息
        NHeader header = message.getHeader();
        if (header.getType() != EnumMessageType.LOGIN_REQ.value()) {
            ctx.fireChannelRead(msg);
            return;
        }

        String clientIp = (String) message.getBody();
        // 重复登录，拒绝
        if (nodeCheck.containsKey(clientIp) || !whiteList.contains(clientIp)) {
            NMessage response = buildResponse((byte)-1);
            ctx.writeAndFlush(response);
            return;
        }
        
        nodeCheck.put(clientIp, true);
        NMessage response = buildResponse((byte) 1);
        ctx.writeAndFlush(response);
    }

    private NMessage buildResponse(byte body) {
        NMessage message = new NMessage();
        NHeader header = new NHeader();
        header.setType(EnumMessageType.LOGIN_RESP.value());
        message.setHeader(header);
        message.setBody(body);
        return message;
    }
}
