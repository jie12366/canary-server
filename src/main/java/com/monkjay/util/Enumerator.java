package com.monkjay.util;

import java.util.*;

/**
 * @author monkJay
 * @description 枚举类的迭代器
 * @date 2020/2/17 22:07
 */
public class Enumerator implements Enumeration {

    public Enumerator(Collection collection) {
        this(collection.iterator());
    }

    public Enumerator(Iterator iterator) {
        super();
        this.iterator = iterator;
    }

    public Enumerator(Map map) {
        this(map.values().iterator());
    }

    private Iterator iterator = null;

    @Override
    public boolean hasMoreElements() {
        return (iterator.hasNext());
    }

    @Override
    public Object nextElement() throws NoSuchElementException {
        return (iterator.next());
    }
}