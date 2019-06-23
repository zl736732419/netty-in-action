package com.zheng.nettyinaction.timeserver.server;

import com.zheng.nettyinaction.timeserver.constants.TimeServerConstants;
import com.zheng.nettyinaction.timeserver.utils.SocketUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeServerHandler implements Runnable {

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