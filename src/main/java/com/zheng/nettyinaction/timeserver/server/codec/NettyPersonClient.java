package com.zheng.nettyinaction.timeserver.server.codec;

import com.zheng.nettyinaction.codecstudy.Person;
import com.zheng.nettyinaction.codecstudy.protobuf.PersonModule;
import com.zheng.nettyinaction.timeserver.constants.TimeServerConstants;
import com.zheng.nettyinaction.timeserver.server.codec.msgpack.MessagePackDecoder;
import com.zheng.nettyinaction.timeserver.server.codec.msgpack.MessagePackEncoder;
import io.netty.bootstrap.Bootstrap;
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
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.net.InetSocketAddress;

/**
 * 解决tcp拆包粘包问题
 * @Author zhenglian
 * @Date 2019/6/25
 */
public class NettyPersonClient {
    private EventLoopGroup boss;
    private Bootstrap bootstrap;
    
    public NettyPersonClient() {
        boss = new NioEventLoopGroup(1);
        bootstrap = new Bootstrap();

        bootstrap.group(boss)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new NettyChannalInitializer())
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
        Object object = PersonModule.Person.newBuilder()
                    .setUserId(1L)
                    .setName("zhangsan")
                    .setMaster("大学本科")
                    .build();
//        Object object = new Person(1L, "zhangsan", "大学本科");
        channel.writeAndFlush(object);
    }

    /**
     * 基于长度字段编解码
     */
    private class NettyChannalInitializer extends ChannelInitializer {
        @Override
        protected void initChannel(Channel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
//            msgpackCodec(pipeline);
            protobufCodec(pipeline);
        }

        private void protobufCodec(ChannelPipeline pipeline) {
            pipeline.addLast(new ProtobufVarint32FrameDecoder())
                    .addLast(new ProtobufDecoder(PersonModule.Person.getDefaultInstance()))
                    .addLast(new ProtobufVarint32LengthFieldPrepender())
                    .addLast(new ProtobufEncoder())
                    .addLast(new NettyPersonClientHandler<PersonModule.Person>());
        }
        
        private void msgpackCodec(ChannelPipeline pipeline) {
            pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2))
                    .addLast(new MessagePackDecoder())
                    .addLast(new LengthFieldPrepender(2))
                    .addLast(new MessagePackEncoder())
                    .addLast(new NettyPersonClientHandler<Person>());
        }
    }
    
    private class NettyPersonClientHandler<T> extends ChannelInboundHandlerAdapter {
        
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            T t = (T) msg;
            System.out.println(t);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }

    }
    
    public static void main(String[] args) {
        NettyPersonClient client = new NettyPersonClient();
        client.connect("localhost", TimeServerConstants.PORT);
    }
}
