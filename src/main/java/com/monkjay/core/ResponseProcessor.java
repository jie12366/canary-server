package com.monkjay.core;

import com.monkjay.connector.http.HttpResponse;
import com.monkjay.util.HttpUtil;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

/**
 * @author monkJay
 * @description 响应客户端
 * @date 2020/2/15 22:16
 */
public class ResponseProcessor {
    public static void process(SelectionKey key, HttpResponse httpResponse) {
        // 对响应头进行编码
        byte [] resHeader = HttpUtil.encodeResponse(httpResponse);
        // 获取响应体
        byte [] body = httpResponse.getOutPutStream().toByteArray();
        ByteBuffer byteBuffer = ByteBuffer.allocate(resHeader.length + body.length);
        // 将响应头和响应体塞入缓冲
        byteBuffer.put(resHeader);
        byteBuffer.put(body);
        byteBuffer.flip();
        // 将响应报文作为附件绑定到选择键中
        key.attach(byteBuffer);
        // 改变关注事件为写事件
        key.interestOps(SelectionKey.OP_WRITE);
        key.selector().wakeup();
    }
}