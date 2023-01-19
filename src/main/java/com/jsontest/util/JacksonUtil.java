package com.jsontest.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.sql.Time;

public class JacksonUtil {

    public static ObjectMapper mapper;
    static {
        try{
            mapper = initJackson();
        }catch(Exception e){
           throw new RuntimeException(e);
        }
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }

    /**
     * Jackson替换fastjson
     * https://www.cnblogs.com/larva-zhh/p/11544317.html
     *
     * FastJson迁移至Jackson
     * https://github.com/mxcall/mxcall.github.io/blob/master/docs/11_work_code/javaSE/FastJson%E8%BF%81%E7%A7%BB%E8%87%B3Jackson.md
     *
     * https://blog.csdn.net/Xiaowu_First/article/details/123846121
     */
    public static ObjectMapper initJackson(){
        JsonMapper.Builder builder = JsonMapper.builder();

        //Jackson 反序列化配置
        // 属性在json有, entity有, 但标记为ignore注解, 不抛出异常   (默认就是false)
        builder.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        //关闭遇到未知属性抛异常(与fastjson保持兼容）
        builder.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //关闭遇到空对象就失败的特性
        builder.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        //开启允许filed没有引号 (与fastjson保持兼容）
        builder.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        // 支持带单引号的key
        builder.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        // 支持0开头的整数, 如001
        builder.configure(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS, true);
        // 支持回车符号
        builder.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS , true);
        //开启序列化时忽略transient修饰的field (与fastjson保持兼容。在有getter setter时才会起作用。如果没有getter setter时，不会序列化transient字段）
        builder.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);

        builder.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);

        ObjectMapper mapper = builder.build();
        //序列化不包含 null值 (与fastjson保持兼容）
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        SimpleModule module = new SimpleModule();
        module.addSerializer(java.sql.Time.class, new JsonSerializer<Time>() {
            @Override
            public void serialize(Time value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeNumber(value.getTime());
            }
        });
        module.addDeserializer(java.sql.Time.class, new JsonDeserializer<Time>() {
            @Override
            public Time deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return new java.sql.Time(p.getLongValue());
            }
        });
        mapper.registerModule(module);

        //jackson的PolymorphicDeserialization默认是支持Object.class、abstract classes、interfaces属性的AUTO Type，
        // 但是该特性容易导致安全漏洞，强烈建议使用ObjectMapper.disableDefaultTyping()设置为只允许@JsonTypeInfo生效
        //https://www.cnblogs.com/larva-zhh/p/11544317.html
        mapper.deactivateDefaultTyping();

        return mapper;
    }


    public static String writeValueAsString(Object bean) {
        try {
            return getMapper().writeValueAsString(bean);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readValue(String json, Class<T> valueType) {
        if(json == null || json.trim().length() == 0){
            return null;
        }
        try {
            return getMapper().readValue(json, valueType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static JsonNode getJsonNode(String jsonStr) {
        JsonNode jsonNode = null;
        try {
            jsonNode = getMapper().readTree(jsonStr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return jsonNode;
    }
}
