package com.monkjay.connector.http;

import com.monkjay.util.LogUtil;
import com.monkjay.util.PropertiesUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author monkJay
 * @description HTTP连接器 基于NIO实现socket通信
 * @date 2020/2/16 21:32
 */
public class HttpConnector implements Runnable{
    /**
     * 是否停止服务器
     */
    private boolean stopped = false;
    /**
     * 服务器端口
     */
    private final int port;

    public HttpConnector() {
        // 如果设置了端口就用指定的端口，如果没有设置端口就使用默认端口8080
        this.port = Integer.parseInt(PropertiesUtil.getValue("port", "8080"));
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
    public String getScheme() {
        // 接收请求的协议为HTTP/1.1
        return  "HTTP/1.1";
    }

    @Override
    public void run() {
        try {
            // 打开选择器
            Selector selector = Selector.open();
            // 获取服务器监听通道
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            // 监听端口
            serverSocketChannel.bind(new InetSocketAddress(port));
            // 设置通道为非阻塞
            serverSocketChannel.configureBlocking(false);
            LogUtil.info("服务器启动，监听地址：[{}]", serverSocketChannel.getLocalAddress());
            while (!stopped) {
                // 接收新连接
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel != null) {
                    // 设置通道非阻塞
                    socketChannel.configureBlocking(false);
                    // 注册到选择器中，并对读事件interest
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }
                // 创建一个请求/响应处理器，并把连接器传给它
                HttpProcessor processor = new HttpProcessor(this);
                // 处理客户端的IO事件
                service(selector, processor);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 对客户端通道进行事件处理
     */
    private void service(Selector selector, HttpProcessor processor) {
        try {
            // 不断调用poll进行轮询，保存就绪的事件
            if (selector.selectNow() > 0) {
                // 遍历选择键集合
                for (SelectionKey selectionKey : selector.selectedKeys()) {
                    // 将事件分发给对应的处理器进行处理
                    if (selectionKey.isReadable()) {
                        // 处理请求
                        processor.processRequest(selectionKey);
                    } else if (selectionKey.isWritable()) {
                        // 处理响应
                        processor.processResponse(selectionKey);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void start() {
        new Thread(new HttpConnector()).start();
    }
}