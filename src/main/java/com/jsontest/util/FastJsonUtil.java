package com.jsontest.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.lang.reflect.Type;

public class FastJsonUtil {

    public static int SerializeFeature = JSON.DEFAULT_GENERATE_FEATURE;   //toJsonString 用的
    public static int DeserializeFeature = JSON.DEFAULT_PARSER_FEATURE;   //parseObject 用的

    static {
        initFastJson();

        boolean printFeature = false;
        if (printFeature) {
            outputFeature();
        }
    }

    public static void initFastJson() {
        // 关闭 fastjson的按字母排序的默认特性。改为按字段定义顺序 (gson 和 jackson 都是默认按字段定义顺序）
        SerializeFeature &= ~SerializerFeature.SortField.getMask();
        // 非字符串的key加上引号
        SerializeFeature |= SerializerFeature.WriteNonStringKeyAsString.getMask();
        // 关闭循环引用检测 （重复引用 不使用 $ref， 但循环引用会抛异常)
        SerializeFeature |= SerializerFeature.DisableCircularReferenceDetect.getMask();
        // 开启序列化时忽略没有实际属性对应的getter方法
        SerializeFeature |= SerializerFeature.IgnoreNonFieldGetter.getMask();
        // 序列化时忽略会抛异常的getter方法
        SerializeFeature |= SerializerFeature.IgnoreErrorGetter.getMask();



        /*** 反序列化特性 ***/

        //关闭将json中的浮点数解析成BigDecimal对象的特性。禁用后会解析成Double对象
        DeserializeFeature &= ~Feature.UseBigDecimal.getMask();
//        解析后属性保持原来的顺序
//        DeserializeFeature |= Feature.OrderedField.getMask();
//         允许json字段名不被引号包括起来 (fastjson 默认开启，但jackson默认关闭）
//        Feature.AllowUnQuotedFieldNames;
//        允许json字段名使用单引号包括起来 (fastjson 默认开启，但jackson默认关闭）
//        Feature.AllowSingleQuotes


        //开启safeMode 完全关闭autoType功能，避免autoType漏洞
        ParserConfig.getGlobalInstance().setSafeMode(true);

    }

    public static String toJSONString(Object bean) {
        return JSON.toJSONString(bean, SerializeFeature);
    }

    public static <T> T parseObject(String json, Type clazz) {
        return JSON.parseObject(json, clazz, DeserializeFeature);
    }

    public static <T> T parseObject(String json, Type clazz, Feature... features) {
        return JSON.parseObject(json, clazz, DeserializeFeature, features);
    }

    private static Object parse(String json, Feature... features) {
        int featureValues = DeserializeFeature;
        for (Feature feature : features) {
            featureValues = Feature.config(featureValues, feature, true);
        }
        return JSON.parse(json, featureValues);
    }

    public static void outputFeature(){

        for (SerializerFeature feature : SerializerFeature.values()) {
            if (SerializerFeature.isEnabled( JSON.DEFAULT_GENERATE_FEATURE, feature)) {
                System.out.println("Default_SerialFeature:" + feature);
            }
        }
        for (SerializerFeature feature : SerializerFeature.values()) {
            if (SerializerFeature.isEnabled(SerializeFeature, feature)) {
                System.out.println("SerialFeature:" + feature);
            }
        }
        System.out.println();

        for (Feature feature : Feature.values()) {
            if (Feature.isEnabled(JSON.DEFAULT_PARSER_FEATURE, feature)) {
                System.out.println("Default_DeserFeature:" + feature);
            }
        }
        for (Feature feature : Feature.values()) {
            if (Feature.isEnabled(DeserializeFeature, feature)) {
                System.out.println("DeserFeature:" + feature);
            }
        }
    }
}
