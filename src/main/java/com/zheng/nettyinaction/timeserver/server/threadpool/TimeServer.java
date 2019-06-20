package com.zheng.nettyinaction.timeserver.server.threadpool;

import com.zheng.nettyinaction.timeserver.constants.TimeServerConstants;
import com.zheng.nettyinaction.timeserver.server.TimeServerHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 伪异步socket编程，对客户端连接处理通过包装成任务由线程池执行
 * 线程:客户端连接 = M:N
 * 从一定程度上改良了传统socket通信
 * 缺点：
 * 数据读写还是通过同步阻塞的stream进行
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
            
            TimeServerHandlerExecutePool pool = new TimeServerHandlerExecutePool(4, 100);
            while (true) {
                socket = serverSocket.accept();
                pool.execute(new TimeServerHandler(socket));
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
