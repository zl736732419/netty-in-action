package com.zheng.nettyinaction.timeserver.server.example05;

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
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import java.net.InetSocketAddress;

/**
 * 还原tcp拆包粘包问题
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
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        // 因为涉及到阻塞操作，所以需要新开一个线程组执行，不能阻塞Netty IO线程
                        pipeline.addLast(new DefaultEventExecutorGroup(1), new NettyTimeClientHandler());
                    }
                })
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

    private void inputCommand(Channel channel) {
        // 发送请求命令
        String command = TimeServerConstants.QUERY_TIME_ORDER;
        for (int i = 0; i < 100; i++) {
            doWrite(channel, command);
        }
    }


    private void doWrite(Channel channel, String line) {
        byte[] bytes = line.getBytes(CharsetUtil.UTF_8);
        ByteBuf buf = Unpooled.copiedBuffer(bytes);
        channel.writeAndFlush(buf);
    }
    private class NettyTimeClientHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf buf = (ByteBuf) msg;
            String response = buf.toString(CharsetUtil.UTF_8);
            System.out.println("Now is " + response + ", current counter is " + ++counter);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }

    }
    
    public static void main(String[] args) {
        NettyTimeCountClient client = new NettyTimeCountClient();
        client.connect("localhost", TimeServerConstants.PORT);
    }
}
