package com.zheng.nettyinaction.timeserver.server.example06;

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
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.LineEncoder;
import io.netty.handler.codec.string.LineSeparator;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 解决粘包拆包问题
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
//                .childHandler(new LineBasedChannalInitializer())
                .childHandler(new DelimiterBasedChannalInitializer())
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
    private class NettyTimeServerHandler extends ChannelInboundHandlerAdapter {

        private String seperator;
        
        private int counter;

        public NettyTimeServerHandler() {
        }
        
        public NettyTimeServerHandler(String seperator) {
            this.seperator = seperator;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            String command = (String) msg;
            System.out.println("server receive command: " + command + ", current counter is " + ++counter);
            String currentTime = (TimeServerConstants.QUERY_TIME_ORDER.equalsIgnoreCase(command))
                    ? LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    : TimeServerConstants.BAD_ORDER;
            
            String response = currentTime;
            if (StringUtils.isNotEmpty(seperator)) {
                response = new StringBuilder(currentTime).append(seperator).toString();
            }
            
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
                    .addLast(new NettyTimeServerHandler());
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
                    .addLast(new NettyTimeServerHandler());
        }
    }
}
