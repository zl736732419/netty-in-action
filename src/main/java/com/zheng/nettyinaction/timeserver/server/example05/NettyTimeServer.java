package com.zheng.nettyinaction.timeserver.server.example05;

import com.zheng.nettyinaction.timeserver.constants.TimeServerConstants;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author zhenglian
 * @Date 2019/6/25
 */
public class NettyTimeServer {
    private EventLoopGroup boss;
    private EventLoopGroup worker;
    private ServerBootstrap bootstrap;
    
    public NettyTimeServer(int port) {
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
                        pipeline.addLast(new NettyTimeServerHandler());
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

    /**
     * 没有考虑粘包拆包，半包读写情况
     */
    private class NettyTimeServerHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf buf = (ByteBuf) msg;
            String command = buf.toString(CharsetUtil.UTF_8).trim();
            System.out.println("server receive command: " + command);
            String currentTime = (TimeServerConstants.QUERY_TIME_ORDER.equalsIgnoreCase(command))
                    ? LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    : TimeServerConstants.BAD_ORDER;
            
            ByteBuf response = Unpooled.copiedBuffer(currentTime.getBytes(CharsetUtil.UTF_8));
            ctx.writeAndFlush(response);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }
    }
    
    public static void main(String[] args) {
        int port = TimeServerConstants.PORT;
        NettyTimeServer server = new NettyTimeServer(port);
        System.out.println("server listening on " + port);
        server.start();
    }
}
