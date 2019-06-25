package com.zheng.nettyinaction.timeserver.server.example03;

import com.zheng.nettyinaction.timeserver.constants.TimeServerConstants;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * nio 编程
 *
 * @Author zhenglian
 * @Date 2019/6/23
 */
public class NioTimeServer extends AbstractNioEventLoop {
    
    public NioTimeServer(int port) {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public void stop() {
        this.stop = true;
    }

    public void start() {
        loop();
    }

    protected void handleInput(SelectionKey key) throws Exception {
        if (null == key || !key.isValid()) {
            return;
        }
        
        if (key.isAcceptable()) {
            handleAccept(key);
        } else if (key.isReadable()) {
            handleRead(key);
        }
    }

    private void handleRead(SelectionKey key) throws Exception {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        
        int readBytes = socketChannel.read(buffer);
        if (readBytes > 0) {
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            String command = new String(bytes, CharsetUtil.UTF_8).trim();
            System.out.println("time server receive order: " + command);
            
            String currentTime = (TimeServerConstants.QUERY_TIME_ORDER.equalsIgnoreCase(command))
                    ? LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    : TimeServerConstants.BAD_ORDER;
            doWrite(socketChannel, currentTime);
            
        } else if (readBytes < 0) { // 连接异常
            key.cancel();
            socketChannel.close();
        }
    }

    private void handleAccept(SelectionKey key) throws Exception {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    public static void main(String[] args) {
        NioTimeServer server = new NioTimeServer(TimeServerConstants.PORT);
        System.out.println("sever listening on " + TimeServerConstants.PORT);
        server.start();
    }
}
