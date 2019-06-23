package com.zheng.nettyinaction.timeserver.utils;

import java.io.IOException;
import java.nio.channels.Selector;

/**
 * @Author zhenglian
 * @Date 2019/6/23
 */
public class NioUtil {
    
    public static void close(Selector selector) {
        // 关闭资源
        if (null != selector) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}
