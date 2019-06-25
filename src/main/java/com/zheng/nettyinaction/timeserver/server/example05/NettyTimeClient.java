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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

/**
 * @Author zhenglian
 * @Date 2019/6/25
 */
public class NettyTimeClient {
    private EventLoopGroup boss;
    private Bootstrap bootstrap;
    
    
    public NettyTimeClient() {
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

    private void inputCommand(Channel channel) throws Exception {
        // 发送请求命令
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while (true) {
            System.out.println("waiting for client input command: ");
            line = reader.readLine();
            if (TimeServerConstants.EXIT_ORDER.equalsIgnoreCase(line)) {
                break;
            }
            doWrite(channel, line);
        }
        // 完成指令，退出程序
        boss.shutdownGracefully();
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
            System.out.println(response);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }

    }
    
    public static void main(String[] args) {
        NettyTimeClient client = new NettyTimeClient();
        client.connect("localhost", TimeServerConstants.PORT);
    }
}
