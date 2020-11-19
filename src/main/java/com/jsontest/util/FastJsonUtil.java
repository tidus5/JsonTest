package com.jsontest.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.jsontest.bean.JsonBean;

import java.lang.reflect.Type;

public class FastJsonUtil {

    public static int SerializeFeature = JSON.DEFAULT_GENERATE_FEATURE;   //toJsonString 用的
    public static int DeserializeFeature = JSON.DEFAULT_PARSER_FEATURE;   //parseObject 用的

    static {
        initFastJson();
    }

    public static void initFastJson() {
        // map的字段按字母顺序排序
        SerializeFeature |= SerializerFeature.MapSortField.getMask();
        //关闭fastjson的按字母排序的默认特性，改为按字段定义顺序 (gson 和 jackson 都是默认按字段定义顺序）
        SerializeFeature &= ~SerializerFeature.SortField.getMask();

        // 非字符串的key加上引号
        SerializeFeature |= SerializerFeature.WriteNonStringKeyAsString.getMask();
        //  关闭循环引用检测 （重复引用 不使用 $ref， 但循环引用会抛异常)
        SerializeFeature |= SerializerFeature.DisableCircularReferenceDetect.getMask();
        //不输出为null 的字段
        SerializeFeature &= ~SerializerFeature.WriteMapNullValue.getMask();
        //序列化时忽略transient修饰的field
//        fastJsonFeature |= SerializerFeature.SkipTransientField.getMask();


        //////////////////////////////////////////////////////////////////////////////////

//        fastJsonDeserFeature &= ~Feature.SortFeidFastMatch.getMask();

        //  将json中的浮点数解析成BigDecimal对象，禁用后会解析成Double对象
        DeserializeFeature &= ~Feature.UseBigDecimal.getMask();
    }

    public static String toJSONString(JsonBean bean) {
        return JSON.toJSONString(bean, SerializeFeature);
    }

    public static <T> T parseObject(String json, Type clazz) {
        return JSON.parseObject(json, clazz, DeserializeFeature);
    }

}
