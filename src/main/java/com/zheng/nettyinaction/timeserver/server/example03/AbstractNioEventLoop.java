package com.zheng.nettyinaction.timeserver.server.example03;

import com.zheng.nettyinaction.timeserver.utils.NioUtil;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @Author zhenglian
 * @Date 2019/6/23
 */
public abstract class AbstractNioEventLoop {
    protected volatile boolean stop;
    protected Selector selector;
    
    public AbstractNioEventLoop() {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public void stop() {
        this.stop = true;
    }
    
    public void loop() {
        while (!stop) {
            try {
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                SelectionKey key;
                while (iterator.hasNext()) {
                    key = iterator.next();
                    iterator.remove();

                    try {
                        handleInput(key);
                    } catch (Exception e) {
                        e.printStackTrace();

                        if (null != key) {
                            key.cancel();
                        }
                        if (null != key.channel()) {
                            key.channel().close();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        NioUtil.close(selector);
    }

    protected void doWrite(SocketChannel socketChannel, String command) throws Exception {
        // 加行尾分隔符是为了兼容socket BufferedReader
        String response = new StringBuilder(command).append("\r\n").toString();
        byte[] bytes = response.getBytes(CharsetUtil.UTF_8);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }
        System.out.println("send " + command + " succeed");
    }
    
    protected abstract void handleInput(SelectionKey key) throws Exception;
}
