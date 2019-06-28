package com.zheng.nettyinaction.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.StringUtils;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.regex.Pattern;

/**
 * <pre>
 *
 *  File:
 *
 *  Copyright (c) 2016, globalegrow.com All Rights Reserved.
 *
 *  Description:
 *  网络文件服务器
 *
 *  Revision History
 *  Date,					Who,					What;
 *  2019年06月28日			zhenglian			    Initial.
 *
 * </pre>
 */
public class HttpFileServer {

    private static final String DEFAULT_PATH = "/src/main/java/";

    public void run(Integer port, String path) {
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        String uri = StringUtils.isEmpty(path) ? DEFAULT_PATH : path;

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline
                                    // 解码http request
                                    .addLast(new HttpRequestDecoder())
                                    // 将多个http请求合并成一个完整的FullHttpRequest
                                    .addLast(new HttpObjectAggregator(65535))
                                    // http response编码
                                    .addLast(new HttpResponseEncoder())
                                    // 异步处理大文件传输
                                    .addLast(new ChunkedWriteHandler()) 
                                    .addLast(new HttpFileServerHandler(uri));
                        }
                    });
            ChannelFuture future = bootstrap.bind(new InetSocketAddress(port)).sync();
            System.out.println("http file server listening on " + port);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    private static class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

        private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

        private String url;

        public HttpFileServerHandler(String url) {
            this.url = url;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
            // request decode failure
            if (request.decoderResult().isFailure()) {
                sendError(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }

            // GET method allowed only
            if (request.method() != HttpMethod.GET) {
                sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
                return;
            }
            
            final String path = sanitizeUri(request.uri());
            if (StringUtils.isEmpty(path)) {
                sendError(ctx, HttpResponseStatus.FORBIDDEN);
                return;
            }
            
            File file = new File(path);
            if (file.isHidden() || !file.exists()) {
                sendError(ctx, HttpResponseStatus.NOT_FOUND);
                return;
            }
            
            if (file.isDirectory()) {
                // TODO
                return;
            }
            
            if (!file.isFile()) {
                sendError(ctx, HttpResponseStatus.FORBIDDEN);
                return;
            }

            RandomAccessFile randomAccessFile;
            try {
                randomAccessFile = new RandomAccessFile(file, "r");
            } catch (Exception e) {
                sendError(ctx, HttpResponseStatus.NOT_FOUND);
                return;
            }

            long length = randomAccessFile.length();
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            setContentLength(response, length);
            setContentTypeHeader(response, file);
            
            // TODO

            
            
        }

        private void setContentTypeHeader(HttpResponse response, File file) {
            MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
            response.headers().add(HttpHeaderNames.CONTENT_TYPE, mimetypesFileTypeMap.getContentType(file.getPath()));
        }

        private void setContentLength(HttpResponse response, long length) {
            response.headers().add(HttpHeaderNames.CONTENT_LENGTH, length);
        }

        private String sanitizeUri(String uri) {
            // url decode
            try {
                uri = URLDecoder.decode(uri, CharsetUtil.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                try {
                    uri = URLDecoder.decode(uri, CharsetUtil.ISO_8859_1.name());
                } catch (UnsupportedEncodingException ex) {
                    throw new Error();
                }
            }

            if (!uri.startsWith(url)) {
                return null;
            }

            uri = uri.replace('/', File.separatorChar);
            if (uri.contains(File.separator + ".") || uri.contains("." + File.separator)
                    || uri.startsWith(".") || uri.endsWith(".")
                    || INSECURE_URI.matcher(uri).matches()) {
                return null;
            }

            return System.getProperty("user.dir") + uri;
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            if (ctx.channel().isActive()) {
                sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            }
        }

        private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                    Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));
            response.headers().add(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=utf-8");
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        }
    }

    public static void main(String[] args) {
//        System.out.println(System.getProperty("user.dir"));
        HttpFileServer server = new HttpFileServer();
        server.run(8888, null);
    }
}
