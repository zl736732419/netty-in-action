package com.zheng.nettyinaction.timeserver.utils;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;

/**
 * @Author zhenglian
 * @Date 2019/6/20
 */
public class SocketUtil {
    
    /**
     * 关闭资源，对于socket顺序为 stream -> socket
     * @param closeables 
     */
    public static void close(Closeable... closeables) {
        if (null == closeables || closeables.length == 0) {
            return;
        }
        Arrays.stream(closeables)
                .filter(closeable -> null != closeable)
                .forEach(closeable -> {
                    try {
                        closeable.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
}
