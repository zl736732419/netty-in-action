package com.zheng.nettyinaction.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
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
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedFile;
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

        private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

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
            
            String uri = request.uri();
            final String path = sanitizeUri(uri);
            System.out.println("path: " + path);
            if (StringUtils.isEmpty(path)) {
                sendError(ctx, HttpResponseStatus.FORBIDDEN);
                return;
            }
            
            File file = new File(path);
            if (file.isHidden() || !file.exists()) {
                sendError(ctx, HttpResponseStatus.NOT_FOUND);
                return;
            }
            
            // handle dir
            if (file.isDirectory()) {
                if (uri.endsWith("/")) {
                    sendListing(ctx, file);
                } else {
                    sendRedirect(ctx, uri + '/');
                }
                return;
            }
            
            // not file type ??? invalid file
            if (!file.isFile()) {
                sendError(ctx, HttpResponseStatus.FORBIDDEN);
                return;
            }

            if (!sendHttpResponse(ctx, request, file)) {
                return;
            }

            sendHttpContent(ctx, request, file);
        }

        private static void sendListing(ChannelHandlerContext ctx, File dir) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
            StringBuilder buf = new StringBuilder();
            String dirPath = dir.getPath();
            buf.append("<!DOCTYPE html>\r\n");
            buf.append("<html><head><title>");
            buf.append(dirPath);
            buf.append(" 目录：");
            buf.append("</title></head><body>\r\n");
            buf.append("<h3>");
            buf.append(dirPath).append(" 目录：");
            buf.append("</h3>\r\n");
            buf.append("<ul>");
            buf.append("<li>链接：<a href=\"../\">..</a></li>\r\n");
            for (File f : dir.listFiles()) {
                if (f.isHidden() || !f.canRead()) {
                    continue;
                }
                String name = f.getName();
                if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
                    continue;
                }
                buf.append("<li>链接：<a href=\"");
                buf.append(name);
                buf.append("\">");
                buf.append(name);
                buf.append("</a></li>\r\n");
            }
            buf.append("</ul></body></html>\r\n");
            ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
            response.content().writeBytes(buffer);
            buffer.release();
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }

        private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
            response.headers().set(HttpHeaderNames.LOCATION, newUri);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }


        private void sendHttpContent(ChannelHandlerContext ctx, FullHttpRequest request, File file) throws Exception {
            // http response  
            RandomAccessFile randomAccessFile;
            try {
                randomAccessFile = new RandomAccessFile(file, "r");
            } catch (Exception e) {
                sendError(ctx, HttpResponseStatus.NOT_FOUND);
                return;
            }

            ChannelFuture future = ctx.write(new ChunkedFile(randomAccessFile, 0, randomAccessFile.length(),
                            8192), ctx.newProgressivePromise());
            future.addListener(new ChannelProgressiveFutureListener() {
                @Override
                public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) throws Exception {
                    if (total < 0) { // total unknown
                        System.err.println("Transfer progress: " + progress);
                    } else {
                        System.err.println("Transfer progress: " + progress + " / "
                                + total);
                    }
                }

                @Override
                public void operationComplete(ChannelProgressiveFuture future) throws Exception {
                    System.out.println("Transfer complete.");
                }
            });
            ChannelFuture lastContentFuture = ctx
                    .writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            
            if (!HttpUtil.isKeepAlive(request)) {
                lastContentFuture.addListener(ChannelFutureListener.CLOSE);
            }
        }

        private boolean sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest request, File file) throws Exception {
            
            long length = file.length();
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            // content-length
            HttpUtil.setContentLength(response, length);
            // content-type
            setContentTypeHeader(response, file);
            // keep alive
            if (HttpUtil.isKeepAlive(request)) {
                HttpUtil.setKeepAlive(response, true);
            }
            ctx.write(response);
            return true;
        }

        private void setContentTypeHeader(HttpResponse response, File file) {
            MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
            response.headers().add(HttpHeaderNames.CONTENT_TYPE, mimetypesFileTypeMap.getContentType(file.getPath()));
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
