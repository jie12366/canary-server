package com.monkjay.connector.http;

import com.monkjay.core.ServletProcessor;
import com.monkjay.core.StaticResourceProcessor;
import com.monkjay.util.HttpUtil;
import com.monkjay.util.LogUtil;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * @author monkJay
 * @description 接收请求和发送响应
 * @date 2020/2/16 21:40
 */
public class HttpProcessor {

    /**
     * 缓冲区
     */
    private ByteBuffer buffer = ByteBuffer.allocate(8192);
    /**
     * HTTP连接器
     */
    private HttpConnector connector;

    public HttpProcessor(HttpConnector connector) {
        this.connector = connector;
    }

    /**
     * 读取客户端请求并进行解析
     * @param key 绑定该客户端通道的选择器
     */
    public void processRequest(SelectionKey key) {
        // 获取客户端通道
        SocketChannel client = (SocketChannel) key.channel();
        // 将请求内容写到缓冲区中
        int len = 0;
        try {
            len = client.read(buffer);
            if (len > 0) {
                buffer.flip();
                // 将buffer中的内容读到字节数组中
                byte[] bytes = new byte[buffer.limit()];
                buffer.get(bytes);
                // 解析HTTP请求内容
                HttpRequest httpRequest = parseRequest(bytes);
                String path = httpRequest.getRequestURI();
                HttpResponse httpResponse = new HttpResponse();
                if (path.startsWith("/servlet")) {
                    ServletProcessor servletProcessor = new ServletProcessor();
                    servletProcessor.process(httpRequest, httpResponse, key);
                } else {
                    StaticResourceProcessor resourceProcessor = new StaticResourceProcessor();
                    resourceProcessor.process(httpRequest, httpResponse, key);
                }
            } else {
                // 请求错误，关闭客户端通道，取消选择键
                client.close();
                key.cancel();
            }
        } catch (IOException | ServletException e) {
            e.printStackTrace();
        }
        // 清空缓冲区
        buffer.clear();
    }

    /**
     * 解析http请求报文
     * @param buffers 报文内容的字节数组
     * @return Request
     */
    public static HttpRequest parseRequest(byte [] buffers) throws ServletException {
        HttpRequest httpRequest = new HttpRequest();
        if (buffers != null) {
            String content = new String(buffers);
            // 将请求报文按行分割为一个数组
            String[] message = content.trim().split("\r\n");
            if (message.length > 0) {
                // 第一行是请求行
                String requestHead = message[0];
                // 按空格分割字符串，分别是 method uri 协议版本
                String[] mainInfo = requestHead.split("\\s+");
                httpRequest.setMethod(mainInfo[0]);
                String path = null;
                try {
                    path = URLDecoder.decode(mainInfo[1], "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    LogUtil.error("URLDecode 解析错误, uri = {}", mainInfo[1], e);
                }
                // 设置HTTP协议
                httpRequest.setProtocol(mainInfo[2]);

                // 解析URI
                parseUri(path, httpRequest);

                // 将请求头存入map中
                Map<String, String> headersMap = new HashMap<>(16);
                for (int i = 1; i < message.length; i++) {
                    String entryStr = message[i];
                    String[] entry = entryStr.trim().split(":");
                    headersMap.put(entry[0].trim(), entry[1].trim());
                }
                httpRequest.setHeaders(headersMap);
                // 解析请求头
                HttpUtil.parseHeaders(headersMap, httpRequest);
            }
        }
        return httpRequest;
    }

    /**
     * 解析URI
     * @param path 请求路径
     * @param httpRequest HttpRequest
     * @throws ServletException
     */
    private static void parseUri(String path, HttpRequest httpRequest) throws ServletException {
        String uri = null;
        if (path != null) {
            int indexOfParam = path.indexOf("?");
            if (indexOfParam > 0) {
                // 获取uri和请求参数
                String queryString = path.substring(indexOfParam + 1);
                httpRequest.setQueryString(queryString);
                uri = path.substring(0, indexOfParam);
            } else {
                httpRequest.setQueryString(null);
                uri = path;
            }
        }

        // 检查绝对URI(如果URI不是一个相对资源)
        if (uri != null && !uri.startsWith("/")) {
            int pos = uri.indexOf("://");
            // 解析协议和主机名
            if (pos != -1) {
                // 从绝对URI中截取出相对资源
                pos = uri.indexOf('/', pos + 3);
                if (pos == -1) {
                    uri = "";
                } else {
                    uri = uri.substring(pos);
                }
            }
        }
        // 从请求URI中解析任何请求的会话ID
        String match = ";jsessionid=";
        assert uri != null;
        int semicolon = uri.indexOf(match);
        if (semicolon >= 0) {
            String rest = uri.substring(semicolon + match.length());
            int semicolon2 = rest.indexOf(';');
            if (semicolon2 >= 0) {
                httpRequest.setRequestedSessionId(rest.substring(0, semicolon2));
                rest = rest.substring(semicolon2);
            }
            else {
                httpRequest.setRequestedSessionId(rest);
                rest = "";
            }
            httpRequest.setRequestedSessionURL(true);
            uri = uri.substring(0, semicolon) + rest;
        }
        else {
            httpRequest.setRequestedSessionId(null);
            httpRequest.setRequestedSessionURL(false);
        }

        // 标准化URI
        String normalizedUri = HttpUtil.normalize(uri);
        if (normalizedUri != null) {
            httpRequest.setRequestURI(normalizedUri);
        } else {
            httpRequest.setRequestURI(uri);
        }
        if (normalizedUri == null) {
            throw new ServletException("Invalid URI: " + uri + "'");
        }
    }

    /**
     * 将响应报文写回到客户端
     * @param key 绑定该客户端通道的选择器
     */
    public void processResponse(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();
        // 取出绑定的附件(这里是响应报文)
        ByteBuffer output = (ByteBuffer) key.attachment();
        try {
            // 将响应报文写入客户端通道中
            client.write(output);
            output.clear();
            // 关闭客户端通道
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}