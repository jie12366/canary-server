package com.monkjay.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author monkJay
 * @description 参数的映射类，内部有个锁用于保护数据不被用户类修改
 *              这里用final修饰，表示这个类不能被继承
 * @date 2020/2/17 19:37
 */
public final class ParameterMap<K, V> extends HashMap<K, V> {

    /**
     * 当前映射类的锁的状态
     */
    private boolean locked = false;

    /**
     * 用默认的容量和负载因子构造一个空的Map
     */
    public ParameterMap() {
        super();
    }

    /**
     * 用容量和默认负载因子构造一个map
     * @param initialCapacity 容量
     */
    public ParameterMap(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * 用容量和负载因子构造一个map
     * @param initialCapacity 容量
     * @param loadFactor 负载因子
     */
    public ParameterMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * 用一个给定的Map构造一个map
     * @param map 给定的map
     */
    public ParameterMap(Map<? extends K, ? extends V> map) {
        super(map);
    }

    @Override
    public V put(K key, V value) {
        if (locked){
            throw new IllegalStateException("不允许对锁定的ParameterMap进行任何修改");
        }
        return (super.put(key, value));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        if (locked) {
            throw new IllegalStateException("不允许对锁定的ParameterMap进行任何修改");
        }
        super.putAll(map);
    }

    @Override
    public V remove(Object key) {
        if (locked) {
            throw new IllegalStateException("不允许对锁定的ParameterMap进行任何修改");
        }
        return (super.remove(key));
    }

    /**
     * 清空这个map的所有映射
     * @exception IllegalStateException 如果map被锁了
     */
    @Override
    public void clear() {
        if (locked){
            throw new IllegalStateException("不允许对锁定的ParameterMap进行任何修改");
        }
        super.clear();
    }

    /**
     * 返回该映射类的锁的状态
     */
    public boolean isLocked() {

        return (this.locked);

    }

    /**
     * 设置这个映射类的锁的状态
     * @param locked 新的锁的状态
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}