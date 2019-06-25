package com.zheng.nettyinaction.timeserver.server.example04;

import com.zheng.nettyinaction.timeserver.constants.TimeServerConstants;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CountDownLatch;

/**
 * @Author zhenglian
 * @Date 2019/6/24
 */
public class AioTimeServer {

    private AsynchronousServerSocketChannel serverSocketChannel;
    private CountDownLatch latch;

    public AioTimeServer(int port) {
        try {
            serverSocketChannel = AsynchronousServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(port));
            latch = new CountDownLatch(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void start() {
        doAccept();
        try {
            // 等待服务器初始化成功
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void doAccept() {
        serverSocketChannel.accept(this, new AcceptCompletionHandler());
    }

    private class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, AioTimeServer> {

        private AsynchronousSocketChannel channel;
        
        @Override
        public void completed(AsynchronousSocketChannel channel, AioTimeServer server) {
            this.channel = channel;
            // 有客户端进来，需要继续监听
            server.doAccept();
            doRead();
        }

        public void doRead() {
            // 处理消息读取
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            channel.read(buffer, buffer, new ReadComplationHandler(this));
        }

        @Override
        public void failed(Throwable exc, AioTimeServer server) {
            try {
                server.serverSocketChannel.close();
                latch.countDown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private class ReadComplationHandler implements CompletionHandler<Integer, ByteBuffer> {

        private AcceptCompletionHandler handler;

        public ReadComplationHandler(AcceptCompletionHandler handler) {
            this.handler = handler;
        }

        @Override
        public void completed(Integer readBytes, ByteBuffer buffer) {
            if (readBytes <= -1) {
                try {
                    handler.channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // 读取命令
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            String command = new String(bytes, CharsetUtil.UTF_8).trim();

            String currentTime = (TimeServerConstants.QUERY_TIME_ORDER.equalsIgnoreCase(command))
                    ? LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    : TimeServerConstants.BAD_ORDER;
            // 加行尾分隔符是为了兼容socket BufferedReader
            String response = new StringBuilder(currentTime).append("\r\n").toString();

            doWrite(response);
        }

        private void doWrite(String response) {
            if (StringUtils.isEmpty(response) || StringUtils.isEmpty(response.trim())) {
                return;
            }

            byte[] bytes = response.getBytes(CharsetUtil.UTF_8);
            ByteBuffer buffer = ByteBuffer.wrap(bytes);

            handler.channel.write(buffer, buffer, new WriteCompletionHandler(handler));
        }

        @Override
        public void failed(Throwable exc, ByteBuffer buffer) {
            try {
                handler.channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class WriteCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {

        private AcceptCompletionHandler handler;

        public WriteCompletionHandler(AcceptCompletionHandler handler) {
            this.handler = handler;
        }

        @Override
        public void completed(Integer result, ByteBuffer buffer) {
            if (buffer.hasRemaining()) {
                handler.channel.write(buffer, buffer, this);
            } else {
                handler.doRead();
            }
        }

        @Override
        public void failed(Throwable exc, ByteBuffer buffer) {
            try {
                handler.channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        int port = TimeServerConstants.PORT;
        AioTimeServer timeServer = new AioTimeServer(port);
        System.out.println("server listening on " + port);
        timeServer.start();
    }
}
