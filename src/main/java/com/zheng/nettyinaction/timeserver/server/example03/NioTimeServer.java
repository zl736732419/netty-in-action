package com.zheng.nettyinaction.timeserver.server.example03;

import com.zheng.nettyinaction.timeserver.constants.TimeServerConstants;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

/**
 * nio 编程
 * @Author zhenglian
 * @Date 2019/6/23
 */
public class NioTimeServer {
    private int port;
    
    public NioTimeServer(int port) {
        this.port = port;
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(port));
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        
    }
    
    public void start() {
        
    }

    public static void main(String[] args) {
        NioTimeServer server = new NioTimeServer(TimeServerConstants.PORT);
        System.out.println("sever listening on " + TimeServerConstants.PORT);
        server.start();
    }
}
