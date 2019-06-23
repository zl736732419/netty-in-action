package com.zheng.nettyinaction.timeserver.server.example01;

import com.zheng.nettyinaction.timeserver.constants.TimeServerConstants;
import com.zheng.nettyinaction.timeserver.server.TimeServerHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 传统socket编程，对每一个客户端连接都新建一个线程
 * 线程:客户端连接 = 1:1
 * 缺点：
 * 1. 每一个连接创建一个线程，当客户端比较多时，会导致内存耗尽
 * 2. 读写操作通过stream进行，阻塞操作
 * @Author zhenglian
 * @Date 2019/6/20
 */
public class TimeServer {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(TimeServerConstants.PORT);
            System.out.println("server listening on " + TimeServerConstants.PORT);
            Socket socket;
            while (true) {
                socket = serverSocket.accept();
                new Thread(new TimeServerHandler(socket)).start();
            }
        } catch (IOException e) { 
            e.printStackTrace();
        } finally {
            System.out.println("server shutdown now .");
            if (null != serverSocket) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
