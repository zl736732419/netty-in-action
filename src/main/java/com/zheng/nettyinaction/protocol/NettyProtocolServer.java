package com.zheng.nettyinaction.protocol;

import com.zheng.nettyinaction.protocol.bean.NMessage;
import com.zheng.nettyinaction.protocol.codec.NMessageDecoder;
import com.zheng.nettyinaction.protocol.codec.NMessageEncoder;
import com.zheng.nettyinaction.protocol.handlers.LoginAuthRespHandler;
import com.zheng.nettyinaction.timeserver.constants.TimeServerConstants;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;

/**
 * @Author zhenglian
 * @Date 2019/6/25
 */
public class NettyProtocolServer {
    private EventLoopGroup boss;
    private EventLoopGroup worker;
    private ServerBootstrap bootstrap;
    
    public NettyProtocolServer(int port) {
        boss = new NioEventLoopGroup(1);
        worker = new NioEventLoopGroup();
        
        bootstrap = new ServerBootstrap();
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new NMessageDecoder(1024 * 1024, 4, 
                                4, -8, 0))
                                .addLast(new NMessageEncoder())
                                .addLast(new LoginAuthRespHandler())
                        ;
                    }
                })
                .localAddress(new InetSocketAddress("localhost", port))
        ;
    }
    
    private void start() {
        try {
            ChannelFuture future = bootstrap.bind().sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
    
    public static void main(String[] args) {
        int port = TimeServerConstants.PORT;
        NettyProtocolServer server = new NettyProtocolServer(port);
        System.out.println("server listening on " + port);
        server.start();
    }

    private class ServerMessageHandler extends SimpleChannelInboundHandler<NMessage> {
        @Override
        public void channelRead0(ChannelHandlerContext ctx, NMessage msg) throws Exception {
            System.out.println("receive msg: " + msg);
            ctx.writeAndFlush(msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
        }
    }
}
