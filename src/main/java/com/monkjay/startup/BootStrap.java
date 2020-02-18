package com.monkjay.startup;

import com.monkjay.connector.http.HttpConnector;

/**
 * @author monkJay
 * @description 启动类
 * @date 2020/2/13 16:43
 */
public class BootStrap {

    public static void main(String[] args) {
        HttpConnector.start();
    }
}