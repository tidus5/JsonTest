package com.jsontest.bean;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jsontest.util.NetUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.jsontest.util.FastJsonUtil.SerializeFeature;

public class JsonBean2 {
    private static Date date = new Date();


    public TreeSet<Integer> integerSet = new TreeSet<>();
    public TreeMap<String, Integer> stringIntegerMap = Maps.newTreeMap();

    public transient int testTransient = 1;

    public int getTestTransient() {
        return testTransient;
    }

    public void setTestTransient(int testTransient) {
        this.testTransient = testTransient;
    }

    @JsonIgnore
    public int getIsOpen() {
        return 0;
    }

    public JsonBean2() {



    }

    public static JsonBean2 getTestBean() {
        JsonBean2 bean = new JsonBean2();
        return bean;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof JsonBean2) {
            return obj.toString().equals(this.toString());
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        //https://www.cnblogs.com/larva-zhh/p/11544317.html
        //map的字段按字母顺序排序。 默认是关闭的（建议关闭，开启会影响性能）
        //输出字符串时，开启map排序。这里开启为了统一输出格式
        return JSON.toJSONString(this, SerializeFeature, SerializerFeature.MapSortField);
    }
}
