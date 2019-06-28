package com.zheng.nettyinaction.timeserver.server.codec;

import com.zheng.nettyinaction.codecstudy.Person;
import com.zheng.nettyinaction.codecstudy.protobuf.PersonModule;
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
import io.netty.handler.codec.marshalling.DefaultMarshallerProvider;
import io.netty.handler.codec.marshalling.DefaultUnmarshallerProvider;
import io.netty.handler.codec.marshalling.MarshallingDecoder;
import io.netty.handler.codec.marshalling.MarshallingEncoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;

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
                .childHandler(new NettyChannelInitializer())
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
    private class NettyPersonServerHandler<T> extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            T t = (T) msg;
            System.out.println(t);
            ctx.writeAndFlush(t);
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
    private class NettyChannelInitializer extends ChannelInitializer {
        @Override
        protected void initChannel(Channel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
//            msgpackCodec(pipeline);
//            protobufCodec(pipeline);
            marshallingCodec(pipeline);
        }

        private void marshallingCodec(ChannelPipeline pipeline) {
            MarshallerFactory factory = Marshalling.getProvidedMarshallerFactory("serial");
            MarshallingConfiguration config = new MarshallingConfiguration();
            config.setVersion(5);
            pipeline.addLast(new MarshallingDecoder(new DefaultUnmarshallerProvider(factory, config)))
                    .addLast(new MarshallingEncoder(new DefaultMarshallerProvider(factory, config)))
                    .addLast(new NettyPersonServerHandler<Person>());
        }
        
        private void msgpackCodec(ChannelPipeline pipeline) {
            pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2))
                    .addLast(new MessagePackDecoder())
                    .addLast(new LengthFieldPrepender(2))
                    .addLast(new MessagePackEncoder())
                    .addLast(new NettyPersonServerHandler<Person>());
        }

        private void protobufCodec(ChannelPipeline pipeline) {
            pipeline.addLast(new ProtobufVarint32FrameDecoder())
                    .addLast(new ProtobufDecoder(PersonModule.Person.getDefaultInstance()))
                    .addLast(new ProtobufVarint32LengthFieldPrepender())
                    .addLast(new ProtobufEncoder())
                    .addLast(new NettyPersonServerHandler<PersonModule.Person>());
        }
    }
}
