package com.jsontest.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.TreeMap;

import static com.jsontest.util.FastJsonUtil.SerializeFeature;

public class BaseJsonBean {

//    @JsonProperty(index = -2)
    public Object mapObject = new TreeMap<>();

    @JsonProperty(index = -2)
    public int testIntInBase = 123;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof BaseJsonBean) {
            return obj.toString().equals(this.toString());
        }
        return super.equals(obj);
    }

    /**
     * https://www.cnblogs.com/larva-zhh/p/11544317.html
     * map的字段按字母顺序排序。 默认是关闭的（建议关闭，开启会影响性能）
     * 输出字符串时，开启map排序。这里开启为了统一输出格式
     *
     *  bean在没有getter setter时，可以让fastjson 序列化时与其他输出相同，大概率按声明顺序，方便比较序列化结果。
     *  https://github.com/alibaba/fastjson/issues/3115
     */
    @Override
    public String toString() {
        return JSON.toJSONString(this, SerializeFeature, SerializerFeature.MapSortField);
    }
}
