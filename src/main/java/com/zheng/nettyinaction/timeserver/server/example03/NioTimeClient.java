package com.zheng.nettyinaction.timeserver.server.example03;

import com.zheng.nettyinaction.timeserver.constants.TimeServerConstants;
import io.netty.util.CharsetUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * @Author zhenglian
 * @Date 2019/6/23
 */
public class NioTimeClient extends AbstractNioEventLoop {
    private SocketChannel socketChannel;

    public NioTimeClient() {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect(String host, int port) {
        try {
            doConnect(host, port);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        loop();
    }

    private void doConnect(String host, int port) throws Exception {
        boolean result = socketChannel.connect(new InetSocketAddress(host, port));
        if (result) {
            socketChannel.register(selector, SelectionKey.OP_READ);
            write(socketChannel);
        } else {
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        }
    }

    private void write(SocketChannel socketChannel) throws Exception {
        new Thread(() -> {
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            String command;
            try {
                while (true) {
                    System.out.println("waiting for client input command: ");
                    command = input.readLine().trim();
                    if (TimeServerConstants.EXIT_ORDER.equalsIgnoreCase(command)) {
                        break;
                    }
                    doWrite(socketChannel, command);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    socketChannel.close();
                    input.close();
                    stop();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                System.out.println("connect closed!");
            }
        }).start();
    }

    public static void main(String[] args) {
        NioTimeClient client = new NioTimeClient();
        client.connect("localhost", TimeServerConstants.PORT);
    }

    @Override
    protected void handleInput(SelectionKey key) throws Exception {
        if (null == key || !key.isValid()) {
            return;
        }

        SocketChannel socketChannel = (SocketChannel) key.channel();
        if (key.isConnectable()) {
            if (socketChannel.finishConnect()) {
                socketChannel.register(selector, SelectionKey.OP_READ);
                write(socketChannel);
            }
        } else if (key.isReadable()) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int readBytes = socketChannel.read(buffer);

            if (readBytes > 0) {
                buffer.flip();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                String command = new String(bytes, CharsetUtil.UTF_8);
                System.out.println("time server receive order: " + command);

            } else if (readBytes < 0) { // 连接异常
                key.cancel();
                socketChannel.close();
            }
        }
    }
}
