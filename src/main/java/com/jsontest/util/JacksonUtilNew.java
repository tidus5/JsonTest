package com.jsontest.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Time;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;


/**
 * Jackson替换fastjson
 * https://www.cnblogs.com/larva-zhh/p/11544317.html
 *
 * FastJson迁移至Jackson
 * https://github.com/mxcall/mxcall.github.io/blob/master/docs/11_work_code/javaSE/FastJson%E8%BF%81%E7%A7%BB%E8%87%B3Jackson.md
 */
public class JacksonUtilNew {

    /**
     * 单例内部类
     */
    public static class SingletonHolder {
        private static ObjectMapper mapper = null;
        private static Configuration jsonPathConf = null;

        static {
            JsonMapper.Builder builder = JsonMapper.builder();

            //Jackson 反序列化配置
            // 属性在json有, entity有, 但标记为ignore注解, 不抛出异常
            builder.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
            //关闭遇到空对象就失败的特性
            builder.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            //开启允许filed没有引号 (与fastjson保持兼容）
            builder.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            // 支持带单引号的key
            builder.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
            //关闭遇到未知属性抛异常(与fastjson保持兼容）
            builder.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            // int类型为null, 则抛出异常
            builder.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
            // 枚举找不到值, 不抛出异常
            builder.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, false);


            //Jackson 序列化配置
            //开启序列化时忽略transient修饰的field (与fastjson保持兼容。在有getter setter时才会起作用。如果没有getter setter时，不会序列化transient字段）
            builder.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);



            SingletonHolder.mapper = builder.build();
            //序列化不包含 null值 (与fastjson保持兼容）
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            //JsonPath
            Configuration.setDefaults(new Configuration.Defaults() {
                private final JsonProvider jsonProvider = new JacksonJsonProvider(SingletonHolder.mapper);
                private final MappingProvider mappingProvider = new JacksonMappingProvider(SingletonHolder.mapper);

                @Override
                public JsonProvider jsonProvider() {
                    return jsonProvider;
                }

                @Override
                public MappingProvider mappingProvider() {
                    return mappingProvider;
                }

                @Override
                public Set<Option> options() {
                    return EnumSet.noneOf(Option.class);
                }
            });

            jsonPathConf = Configuration.defaultConfiguration()
                    .addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL)
                    .addOptions(Option.SUPPRESS_EXCEPTIONS)
            ;


            SimpleModule sqlTimeModule = new SimpleModule();
            sqlTimeModule.addSerializer(java.sql.Time.class, new JsonSerializer<Time>() {
                @Override
                public void serialize(Time value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    gen.writeNumber(value.getTime());
                }
            });
            sqlTimeModule.addDeserializer(java.sql.Time.class, new JsonDeserializer<Time>() {
                @Override
                public Time deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    return new java.sql.Time(p.getLongValue());
                }
            });


            mapper.registerModule(sqlTimeModule);
            mapper.registerModule(new JsonOrgModule());

            //jackson的PolymorphicDeserialization默认是支持Object.class、abstract classes、interfaces属性的AUTO Type，
            // 但是该特性容易导致安全漏洞，强烈建议使用ObjectMapper.disableDefaultTyping()设置为只允许@JsonTypeInfo生效
            //https://www.cnblogs.com/larva-zhh/p/11544317.html
            mapper.deactivateDefaultTyping();

        }
    }


    public static String writeValueAsString(Object bean) {
        try {
            return getInstance().writeValueAsString(bean);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readValue(String json, Class<T> valueType) {
        try {
            return getInstance().readValue(json, valueType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static JsonNode getJsonNode(String jsonStr) {
        JsonNode jsonNode = null;
        try {
            jsonNode = getInstance().readTree(jsonStr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return jsonNode;
    }



    /****************** FastJson 风格api ******************************************/
    /**
     * 单例get
     * @return
     */
    public static ObjectMapper getInstance(){
        return SingletonHolder.mapper;
    }

    /**
     * clone 一个可以改配置
     * @return
     */
    public static ObjectMapper copyInstance(){
        ObjectMapper copy = SingletonHolder.mapper.copy();
        return copy;
    }

    /**
     * 指定Obj 对象-> 字符串
     * @param obj
     * @param <T>
     * @return
     * @throws JsonProcessingException
     */
    public static <T> String toJSONString(T obj) throws JsonProcessingException {
        return getInstance().writeValueAsString(obj);
    }


    /**
     * 字符串 -> 指定Obj 对象
     * @param json
     * @param valueType
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T parseObject(String json, Class<T> valueType) throws IOException {
        return getInstance().readValue(json, valueType);
    }

    /**
     * 字符串 -> 通用类JSONObject
     * @param json
     * @return
     * @throws IOException
     */
    public static JSONObject parseObject(String json) throws IOException {
        return getInstance().readValue(json, JSONObject.class);

    }


    /**
     * 字符串 -> 指定数组类
     * @param json
     * @param valueType
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> List<T> parseArray(String json, Class<T> valueType) throws IOException {
        return getInstance().readValue(json, new TypeReference<List<T>>(){});
    }


    /**
     * 字符串 -> 通用数组类 JSONArray
     * @param json
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> JSONArray parseArray(String json) throws IOException {
        return getInstance().readValue(json, JSONArray.class);
    }


    /**
     * 高级: 获取JSONPath 帮助类
     * @param jsonStr
     * @return
     * @throws IOException
     */
    public static DocumentContext getDocumentContext(String jsonStr) throws IOException {
        DocumentContext dcHelp = JsonPath.using(SingletonHolder.jsonPathConf).parse(jsonStr);
        return dcHelp;
    }


    /**
     * 高级: 解析 JSONPath
     * @param dcHelp
     * @param path
     * @return
     * @throws IOException
     */
    public static Object evalPath(DocumentContext dcHelp, String path) throws IOException {
        // 参考链接 https://github.com/json-path/JsonPath
        Object result = dcHelp.read(path);
        return result;
    }


    /**
     * 高级: 默认配置解析 JSONPath
     * @param jsonStr
     * @param path
     * @return
     * @throws IOException
     */
    public static Object evalPath(String jsonStr, String path) throws IOException {
        DocumentContext dcHelp = getDocumentContext(jsonStr);
        Object o = evalPath(dcHelp, path);
        return  o;
    }
}
