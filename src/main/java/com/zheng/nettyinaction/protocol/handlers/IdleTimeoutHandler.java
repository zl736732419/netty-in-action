package com.zheng.nettyinaction.protocol.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @Author zhenglian
 * @Date 2019/6/30
 */
public class IdleTimeoutHandler extends ChannelInboundHandlerAdapter {

    /**
     * 空闲超时次数，当大于3次时，关闭连接
     */
    private int timeoutCount;
    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (!(evt instanceof IdleStateEvent)) {
            ctx.fireUserEventTriggered(evt);
            return;
        }

        IdleStateEvent event = (IdleStateEvent) evt;
        if (event.state() == IdleState.READER_IDLE) { // 读空闲
            if (timeoutCount++ > 3) {
                System.out.println("client idle timeout count > 3 times, close connection now.");
                ctx.close();
                return;
            } else {
                System.out.println("idle timeout happen " + timeoutCount + " times now.");
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // 清零空闲超时次数
        timeoutCount = 0;
        super.channelReadComplete(ctx);
    }
}
