package com.zheng.nettyinaction.timeserver.server.codec;

import com.zheng.nettyinaction.codecstudy.Person;
import com.zheng.nettyinaction.timeserver.constants.TimeServerConstants;
import com.zheng.nettyinaction.timeserver.server.codec.msgpack.MessagePackDecoder;
import com.zheng.nettyinaction.timeserver.server.codec.msgpack.MessagePackEncoder;
import io.netty.bootstrap.ServerBootstrap;
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
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;

/**
 * 解决粘包拆包问题
 * @Author zhenglian
 * @Date 2019/6/25
 */
public class NettyPersonServer {
    private EventLoopGroup boss;
    private EventLoopGroup worker;
    private ServerBootstrap bootstrap;

    public NettyPersonServer(int port) {
        boss = new NioEventLoopGroup(1);
        worker = new NioEventLoopGroup();

        bootstrap = new ServerBootstrap();
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new LengthFieldBasedChannalInitializer())
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
     * 考虑粘包拆包，半包读写情况
     */
    private class NettyPersonServerHandler extends ChannelInboundHandlerAdapter {

        public NettyPersonServerHandler() {
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Person person = (Person) msg;
            System.out.println(person);
            ctx.writeAndFlush(person);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }
    }

    public static void main(String[] args) {
        int port = TimeServerConstants.PORT;
        NettyPersonServer server = new NettyPersonServer(port);
        System.out.println("server listening on " + port);
        server.start();
    }

    /**
     * 基于长度字段编解码
     */
    private class LengthFieldBasedChannalInitializer extends ChannelInitializer {
        @Override
        protected void initChannel(Channel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2))
                    .addLast(new MessagePackDecoder())
                    .addLast(new LengthFieldPrepender(2))
                    .addLast(new MessagePackEncoder())
                    .addLast(new NettyPersonServerHandler());
        }
    }
}