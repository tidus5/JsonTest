package com.jsontest.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.concurrent.TimeUnit;

public class SimpleJsonBean extends BaseJsonBean{

//  Gson 字段排除策略总结
//  https://juejin.cn/post/6965524230834225183
    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    public transient int jacksonIgnoreField;


    public transient int testTransient;

    public int testInt;
    public String testString;
    public TimeUnit timeUnit;
}
