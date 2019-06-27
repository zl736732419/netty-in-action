package com.zheng.nettyinaction.timeserver.server.example06;

import com.zheng.nettyinaction.timeserver.constants.TimeServerConstants;
import io.netty.bootstrap.Bootstrap;
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
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.LineEncoder;
import io.netty.handler.codec.string.LineSeparator;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;

/**
 * 解决tcp拆包粘包问题
 * @Author zhenglian
 * @Date 2019/6/25
 */
public class NettyTimeCountClient {
    private EventLoopGroup boss;
    private Bootstrap bootstrap;
    
    private int counter;
    
    public NettyTimeCountClient() {
        boss = new NioEventLoopGroup(1);
        bootstrap = new Bootstrap();

        bootstrap.group(boss)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
//                .handler(new LineBasedChannalInitializer())
//                .handler(new DelimiterBasedChannalInitializer())
                .handler(new LengthFieldBasedChannalInitializer())
        ;
    }

    private void connect(String host, int port) {
        try {
            ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port)).sync();
            inputCommand(future.channel());
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
        }
    }

    /**
     * 基于长度字段编解码
     */
    private class LengthFieldBasedChannalInitializer extends ChannelInitializer {
        @Override
        protected void initChannel(Channel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            // delimiters can use netty Delimiters util instead.
            pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 2, 0, 2))
                    .addLast(new StringDecoder())
                    .addLast(new LengthFieldPrepender(2))
                    .addLast(new StringEncoder())
                    .addLast(new NettyTimeClientHandler());
        }
    }
    
    /**
     * 基于自定义字符编解码
     */
    private class DelimiterBasedChannalInitializer extends ChannelInitializer {
        @Override
        protected void initChannel(Channel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            // delimiters can use netty Delimiters util instead.
            ByteBuf[] delimiters = new ByteBuf[] {Unpooled.wrappedBuffer(new byte[] { '|' }) };
            pipeline.addLast(new DelimiterBasedFrameDecoder(1024, delimiters))
                    .addLast(new StringDecoder())
                    .addLast(new StringEncoder())
                    .addLast(new LineEncoder(new LineSeparator("|")))
                    .addLast(new NettyTimeClientHandler());
        }
    }

    /**
     * 基于行分隔符编解码
     */
    private class LineBasedChannalInitializer extends ChannelInitializer {
        @Override
        protected void initChannel(Channel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new LineBasedFrameDecoder(1024))
                    .addLast(new StringDecoder())
                    .addLast(new StringEncoder())
                    .addLast(new LineEncoder())
                    .addLast(new NettyTimeClientHandler());
        }
    }
    
    private void inputCommand(Channel channel) {
        // 发送请求命令
        String command = TimeServerConstants.QUERY_TIME_ORDER;
        for (int i = 0; i < 100; i++) {
            doWrite(channel, command);
        }
    }


    private void doWrite(Channel channel, String command) {
        channel.writeAndFlush(command);
    }
    private class NettyTimeClientHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            String response = (String) msg;
            System.out.println("Now is " + response + ", current counter is " + ++counter);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }

    }
    
    public static void main(String[] args) {
        NettyTimeCountClient client = new NettyTimeCountClient();
        client.connect("localhost", TimeServerConstants.PORT);
    }
}
