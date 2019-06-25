package com.zheng.nettyinaction.timeserver.server.example04;

import com.zheng.nettyinaction.timeserver.constants.TimeServerConstants;
import io.netty.util.CharsetUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * @Author zhenglian
 * @Date 2019/6/25
 */
public class AioTimeClient {
    
    private AsynchronousSocketChannel socketChannel;
    private CountDownLatch latch;
    private String host;
    private Integer port;

    public AioTimeClient(String host, int port) {
        this.host = host;
        this.port = port;
        latch = new CountDownLatch(1);
        try {
            socketChannel = AsynchronousSocketChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void connect() {
        socketChannel.connect(new InetSocketAddress(host, port), socketChannel, new ConnectCompletionHandler());
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectCompletionHandler implements CompletionHandler<Void, AsynchronousSocketChannel> {
        private AsynchronousSocketChannel channel;

        @Override
        public void completed(Void result, AsynchronousSocketChannel channel) {
            this.channel = channel;
            // 写数据
            new Thread(new ConsoleCommand(channel)).start();
        }

        @Override
        public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
            try {
                channel.close();
                latch.countDown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConsoleCommand implements Runnable {
        private AsynchronousSocketChannel channel;
        
        public ConsoleCommand(AsynchronousSocketChannel channel) {
            this.channel = channel;
        }
        
        @Override
        public void run() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String line;
            try {
                while (true) {
                    System.out.println("waiting for client input command: ");
                    line = reader.readLine();
                    if (TimeServerConstants.EXIT_ORDER.equalsIgnoreCase(line)) {
                        break;
                    }
                    doWrite(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    channel.close();
                    reader.close();
                    latch.countDown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void doWrite(String line) {
            line = new StringBuilder(line).append("\r\n").toString();
            byte[] bytes = line.getBytes(CharsetUtil.UTF_8);
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            channel.write(buffer, buffer, new WriteCompleteHandler(channel));
        }
    }

    private class WriteCompleteHandler implements CompletionHandler<Integer, ByteBuffer>{
        private AsynchronousSocketChannel channel;
        
        public WriteCompleteHandler(AsynchronousSocketChannel channel) {
            this.channel = channel;
        }
        
        @Override
        public void completed(Integer result, ByteBuffer buffer) {
            if (buffer.hasRemaining()) {
                channel.write(buffer, buffer, this);
            } else {
                doRead(channel);
            }
        }

        private void doRead(AsynchronousSocketChannel channel) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            channel.read(buffer, buffer, new ReadCompletionHandler(channel));
        }

        @Override
        public void failed(Throwable exc, ByteBuffer buffer) {
            try {
                channel.close();
                latch.countDown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ReadCompletionHandler implements CompletionHandler<Integer, ByteBuffer>{
        
        private AsynchronousSocketChannel channel;
        
        public ReadCompletionHandler(AsynchronousSocketChannel channel) {
            this.channel = channel;
        }
        
        @Override
        public void completed(Integer readBytes, ByteBuffer buffer) {
            if (readBytes <= -1) {
                try {
                    channel.close();
                    latch.countDown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                buffer.flip();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                String command = new String(bytes, CharsetUtil.UTF_8);
                System.out.println("time server receive order: " + command);  
            }
        }

        @Override
        public void failed(Throwable exc, ByteBuffer buffer) {
            try {
                channel.close();
                latch.countDown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        AioTimeClient client = new AioTimeClient("localhost", TimeServerConstants.PORT);
        client.connect();
    }
}
