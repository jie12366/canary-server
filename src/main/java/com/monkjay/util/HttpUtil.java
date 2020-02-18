package com.monkjay.util;

import com.monkjay.connector.http.HttpResponse;
import com.monkjay.connector.http.HttpRequest;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author monkJay
 * @description HTTP协议的编解码
 * @date 2020/2/13 15:50
 */
public class HttpUtil {

    /**
     * 解析请求头
     * @param headers 请求头映射
     */
    public static void parseHeaders(Map<String, String> headers, HttpRequest request) throws ServletException {
        if (headers.get("cookie") != null) {
            Cookie[] cookies = parseCookieHeader(headers.get("cookie"));
            for (Cookie cookie : cookies) {
                if ("jsessionid".equals(cookie.getName())) {
                    // 覆盖URL中请求的任何内容
                    if (!request.isRequestedSessionIdFromCookie()) {
                        // 仅接受第一个会话ID的Cookie
                        request.setRequestedSessionId(cookie.getValue());
                        request.setRequestedSessionCookie(true);
                        request.setRequestedSessionURL(false);
                    }
                }
                request.addCookie(cookie);
            }
        }
        else if (headers.get("content-length") != null) {
            int n = -1;
            try {
                n = Integer.parseInt(headers.get("content-length"));
            } catch (Exception e) {
                throw new ServletException("非法的请求头的 content-length ");
            }
            request.setContentLength(n);
        }
        else if (headers.get("content-type") != null) {
            request.setContentType(headers.get("content-type"));
        }
    }

    /**
     * 将cookie请求头解析为cookie数组
     * @param header "Cookie" header
     */
    private static Cookie[] parseCookieHeader(String header) {
        if ((header == null) || (header.length() < 1)) {
            return (new Cookie[0]);
        }

        ArrayList<Cookie> cookies = new ArrayList<>();
        while (header.length() > 0) {
            int semicolon = header.indexOf(';');
            if (semicolon < 0) {
                semicolon = header.length();
            }
            if (semicolon == 0) {
                break;
            }
            String token = header.substring(0, semicolon);
            if (semicolon < header.length()) {
                header = header.substring(semicolon + 1);
            } else {
                header = "";
            }
            int equals = token.indexOf('=');
            if (equals > 0) {
                String name = token.substring(0, equals).trim();
                String value = token.substring(equals+1).trim();
                cookies.add(new Cookie(name, value));
            }
        }
        return (cookies.toArray(new Cookie[0]));
    }

    /**
     * 解析参数，并存入映射类中
     * @param map 映射类
     * @param data 要解析的字符串
     */
    public static void parseParameters(Map<String, String[]> map, String data) {
        String[] paramEntrys = data.split("&");
        for (String paramEntry : paramEntrys) {
            String [] entry = paramEntry.split("=");
            if (entry.length > 0) {
                String key = entry[0];
                String value = entry.length > 1 ? entry[1] : "";
                putEntry(map, key, value);
            }
        }
    }

    /**
     * 将键值放入map，如果键已存在，则将值存入数组中
     * @param map 映射类实例
     * @param key 键
     * @param value 值
     */
    private static void putEntry(Map<String, String[]> map, String key, String value) {
        String[] newValues;
        String[] oldValues = map.get(key);
        if (oldValues == null) {
            newValues = new String[1];
            newValues[0] = value;
        } else {
            newValues = new String[oldValues.length + 1];
            System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
            newValues[oldValues.length] = value;
        }
        map.put(key, newValues);
    }

    /**
     * 标准化URI
     * @param path 传入的URI
     * @return 标准化后的URI
     */
    public static String normalize(String path) {
        if (path == null) {
            return null;
        }
        // 创建一个标准化路径
        String normalized = path;
        // 在开头将"/％7E"和"/％7e"规范化为"/~"
        if (normalized.startsWith("/%7E") || normalized.startsWith("/%7e")) {
            normalized = "/~" + normalized.substring(4);
        }
        // 禁止编码"％"，"/"，"."和"\"，这是特殊保留的字符
        if ((normalized.contains("%25")) || (normalized.contains("%2F"))
          || (normalized.contains("%2E")) || (normalized.contains("%5C"))
          || (normalized.contains("%2f"))|| (normalized.contains("%2e")) || (normalized.contains("%5c"))) {
            return null;
        }
        if ("/.".equals(normalized)) {
            return "/";
        }
        // 标准化斜杠，并在必要时添加前导斜杠
        if (normalized.indexOf('\\') >= 0) {
            normalized = normalized.replace('\\', '/');
        }
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        // 解决标准化路径中" //"的出现
        while (true) {
            int index = normalized.indexOf("//");
            if (index < 0) {
                break;
            }
            normalized = normalized.substring(0, index) + normalized.substring(index + 1);
        }
        // 解决标准化路径中"/./"的出现
        while (true) {
            int index = normalized.indexOf("/./");
            if (index < 0) {
                break;
            }
            normalized = normalized.substring(0, index) + normalized.substring(index + 2);
        }
        // 解决标准化路径中"/../"的出现
        while (true) {
            int index = normalized.indexOf("/../");
            if (index < 0) {
                break;
            }
            if (index == 0) {
                return (null);
            }
            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized = normalized.substring(0, index2) + normalized.substring(index + 3);
        }
        //声明出现的"/ ..."（三个或更多点）为无效
        // （在某些Windows平台上，这会走目录树）
        if (normalized.contains("/...")) {
            return (null);
        }
        // 返回已经完成的标准化路径
        return (normalized);
    }

    /**
     * 对http响应头字节流进行编码
     * @param httpResponse Response
     * @return 响应字节流
     */
    public static byte[] encodeResponse(HttpResponse httpResponse) {
        StringBuilder resBuild = new StringBuilder();
        // 响应状态行：HTTP协议版本 状态码 状态码描述
        resBuild.append("HTTP/1.1").append(" ").append(httpResponse.getStatus()).append(" ").append(httpResponse.getMessage());
        resBuild.append("\r\n");

        // 编码响应头部
        Map<String, String> headers = httpResponse.getHeaders();
        headers.forEach((key, value) -> {
            resBuild.append(key);
            resBuild.append(": ");
            resBuild.append(value);
            resBuild.append("\r\n");
        });
        resBuild.append("\r\n");

        // 返回编码的响应头
        return resBuild.toString().getBytes(StandardCharsets.UTF_8);
    }
}
