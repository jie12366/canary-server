package com.monkjay.core;

import com.monkjay.connector.Constants;
import com.monkjay.connector.http.HttpRequest;
import com.monkjay.connector.http.HttpResponse;
import com.monkjay.util.LogUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;

/**
 * @author monkJay
 * @description 静态资源处理器
 * @date 2020/2/15 21:26
 */
public class StaticResourceProcessor {
    public void process(HttpRequest httpRequest, HttpResponse httpResponse, SelectionKey key) {
        String path = httpRequest.getRequestURI();
        try {
            File file = new File(Constants.WEB_ROOT ,path);
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            FileChannel fileChannel = randomAccessFile.getChannel();
            ByteBuffer htmBuffer = ByteBuffer.allocate((int)fileChannel.size());
            fileChannel.read(htmBuffer);
            htmBuffer.flip();
            byte [] htmByte = new byte[htmBuffer.limit()];
            htmBuffer.get(htmByte);
            httpResponse.getOutPutStream().write(htmByte);
        } catch (IOException e) {
            LogUtil.error("静态视图解析服务异常", e);
        }
        // 处理响应
        ResponseProcessor.process(key, httpResponse);
    }
}