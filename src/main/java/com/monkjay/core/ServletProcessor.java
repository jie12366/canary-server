package com.monkjay.core;

import com.monkjay.connector.http.HttpRequest;
import com.monkjay.connector.http.HttpResponse;
import com.monkjay.connector.Constants;
import com.monkjay.util.LogUtil;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.SelectionKey;

/**
 * @author monkJay
 * @description servlet处理器
 * @date 2020/2/15 20:58
 */
public class ServletProcessor {
    public void process(HttpRequest httpRequest, HttpResponse httpResponse, SelectionKey key) {
        // 从路径中获取servlet
        String path = httpRequest.getRequestURI();
        String servletName = path.substring(path.lastIndexOf('/') + 1);
        // 创建一个URLClassLoader 通过路径加载servlet
        URLClassLoader classLoader = null;
        try {
            // 获取servlet的目录路径，并创建一个url
            File classPath = new File(Constants.WEB_ROOT);
            URL url = new URL("file:F:/Middleware/canary/webroot/");
            LogUtil.info("url路径：[{}]", url.toString());
            // 根据url创建一个类加载器
            classLoader = new URLClassLoader(new URL[]{url});
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 用URLClassLoader 根据servlet名称创建一个servlet类
        Class servletClass = null;
        try {
            if (classLoader != null) {
                LogUtil.info("类名：[{}]", servletName);
                servletClass = classLoader.loadClass(servletName);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        // 根据刚刚通过类加载器加载的类反射创建一个servlet实例
        Servlet servlet = null;
        try {
            if (servletClass != null) {
                servlet = (Servlet) servletClass.newInstance();
                // 调用servlet的service方法
                servlet.service(httpRequest, httpResponse);
            }
        } catch (InstantiationException | IllegalAccessException | ServletException | IOException e) {
            e.printStackTrace();
        }
        // 处理响应
        ResponseProcessor.process(key, httpResponse);
    }
}