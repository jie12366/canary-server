package com.monkjay.util;

import org.slf4j.LoggerFactory;

/**
 * @author monkJay
 * @description
 * @date 2020/2/12 22:05
 */
public class LogUtil {

    /**
     * 当前日志工具类的类名
     */
    private final static String LOG_CLASS_NAME = LogUtil.class.getName();

    /**
     * 获取最原始被调用的堆栈信息
     */
    private static StackTraceElement getStackTraceElement() {
        // 获取堆栈信息
        StackTraceElement[] traceElements = Thread.currentThread().getStackTrace();
        if (traceElements == null) {
            return null;
        }
        // 最原始被调用的堆栈信息
        StackTraceElement stackTraceElement = null;
        // 循环遍历到日志类标识
        boolean isEachLogFlag = false;
        // 遍历堆栈信息，获取出最原始被调用的方法信息
        // 当前日志类的堆栈信息完了就是调用该日志类对象信息
        for (StackTraceElement element : traceElements) {
            // 遍历到日志类
            if (element.getClassName().equals(LOG_CLASS_NAME)) {
                isEachLogFlag = true;
            }

            // 下一个非日志类的堆栈，就是最原始被调用的方法
            if (isEachLogFlag) {
                if (!element.getClassName().equals(LOG_CLASS_NAME)) {
                    stackTraceElement = element;
                    break;
                }
            }
        }
        return stackTraceElement;
    }

    /**
     * 自动匹配请求类名，生成logger对象
     */
    private static org.slf4j.Logger log() {
        // 最原始被调用的堆栈对象
        StackTraceElement stackTraceElement = getStackTraceElement();
        // 空堆栈处理
        if (stackTraceElement == null) {
            return LoggerFactory.getLogger(LogUtil.class);
        }

        // 取出被调用对象的类名，并构造一个Logger对象返回
        return LoggerFactory.getLogger(stackTraceElement.getClassName());
    }
    
    public static void info(String message) {
        log().info(message);
    }

    public static void info(String message, Object format) {
        log().info(message, format);
    }

    public static void error(String message, Throwable e) {
        log().error(message, e);
    }

    public static void error(String message, Object format, Throwable e) {
        log().error(message, format, e);
    }
}