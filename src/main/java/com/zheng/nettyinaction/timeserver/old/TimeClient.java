package com.zheng.nettyinaction.timeserver.old;

import com.zheng.nettyinaction.timeserver.constants.TimeServerConstants;
import com.zheng.nettyinaction.timeserver.utils.SocketUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @Author zhenglian
 * @Date 2019/6/20
 */
public class TimeClient {
    public static void main(String[] args) {
        Socket socket = new Socket();

        BufferedReader input = null;
        BufferedReader reader = null;
        PrintWriter out = null;
        try {
            socket.connect(new InetSocketAddress("localhost", TimeServerConstants.PORT));
            input = new BufferedReader(new InputStreamReader(System.in));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
            
            String command;
            while (true) {
                System.out.println("waiting for console type command: ");
                command = input.readLine();
                if (command.equalsIgnoreCase(TimeServerConstants.EXIT_ORDER)) {
                    break;
                }
                out.println(command);
                out.flush();
                System.out.println("Send order 2 client succeed.");
                
                String response = reader.readLine();
                System.out.println("Now is " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            SocketUtil.close(input, reader, out, socket);
        }
    }
}
