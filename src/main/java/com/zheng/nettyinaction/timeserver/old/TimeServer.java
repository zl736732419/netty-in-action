package com.zheng.nettyinaction.timeserver.old;

import com.zheng.nettyinaction.timeserver.constants.TimeServerConstants;
import com.zheng.nettyinaction.timeserver.utils.SocketUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
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

    private static class TimeServerHandler implements Runnable {

        private Socket socket;

        public TimeServerHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            BufferedReader reader = null;
            PrintWriter out = null;
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream());

                String line;
                String currentTime;
                while (true) {
                    line = reader.readLine();
                    if (null == line) {
                        break;
                    }
                    
                    currentTime = (line.equalsIgnoreCase(TimeServerConstants.QUERY_TIME_ORDER)) 
                            ? LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            : TimeServerConstants.BAD_ORDER;
                    out.println(currentTime);
                    out.flush();
                }
            } catch (IOException e) {
                System.out.println("client " + socket.getRemoteSocketAddress() + " exit.");
                SocketUtil.close(reader, out, socket);
            }
        }
    }
}
