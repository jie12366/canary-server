package com.monkjay.exception;

/**
 * 视图解析异常
 * @author monkJay
 * @date 2020/2/13
 */
public class ViewNotFoundException extends RuntimeException {

    public ViewNotFoundException() {
        super("404 页面丢失了！！");
    }
}
