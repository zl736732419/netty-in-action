package com.zheng.nettyinaction.protocol;

import com.zheng.nettyinaction.protocol.codec.NMessageDecoder;
import com.zheng.nettyinaction.protocol.codec.NMessageEncoder;
import com.zheng.nettyinaction.protocol.handlers.HeartBeatReqHandler;
import com.zheng.nettyinaction.protocol.handlers.IdleTimeoutHandler;
import com.zheng.nettyinaction.protocol.handlers.LoginAuthReqHandler;
import com.zheng.nettyinaction.timeserver.constants.TimeServerConstants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author zhenglian
 * @Date 2019/6/25
 */
public class NettyProtocolClient {
    
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    
    private EventLoopGroup boss;
    private Bootstrap bootstrap;
    
    public NettyProtocolClient() {
        boss = new NioEventLoopGroup(1);
        bootstrap = new Bootstrap();

        bootstrap.group(boss)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new NMessageDecoder(1024 * 1024, 
                                4, 4, -8, 0))
                                .addLast(new NMessageEncoder())
                                .addLast(new LoginAuthReqHandler())
                                .addLast(new HeartBeatReqHandler())
                                .addLast(new IdleStateHandler(10, 10, 10))
                                .addLast(new IdleTimeoutHandler())
                        ;
                    }
                })
        ;
    }
    
    private void connect(String host, int port) {
        System.out.println("start connecting server " + host + ":" + port + "...");
        try {
            ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port)).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 发起重连机制
            executor.execute(() -> {
                try {
                    TimeUnit.SECONDS.sleep(5);
                    try {
                        connect(host, port);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

    }

    public static void main(String[] args) {
        NettyProtocolClient client = new NettyProtocolClient();
        client.connect("192.168.3.12", TimeServerConstants.PORT);
    }
}
