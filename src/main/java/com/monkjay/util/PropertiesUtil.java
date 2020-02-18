package com.monkjay.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 从properties文件中获取配置
 * @author monkJay
 * @date 2020/2/13
 */
public class PropertiesUtil {
    /**
     * 服务器配置信息
     */
    private static Properties properties;

    /**
     * 根据key获取配置项
     *
     * @param key 配置的key
     * @param defaultValue 配置的默认值
     * @return 返回配置值
     */
    public static String getValue(String key,String defaultValue){
        ensureProps();
        return properties.getProperty(key,defaultValue);
    }

    /**
     * 根据key获取配置项
     * @param key 配置的key
     * @return 配置的值
     */
    public static String getValue(String key) {
        ensureProps();
        return properties.getProperty(key);
    }

    /**
     * 加载properties配置文件
     */
    private static void ensureProps() {
        if (properties == null) {
            InputStream inputStream = PropertiesUtil.class.getClassLoader().getResourceAsStream("server.properties");
            properties = new Properties();
            try {
                properties.load(inputStream);
            } catch (IOException e) {
                LogUtil.error("error_PropertiesUtil_ensureProps, 异常", e);
            }
        }
    }
}
