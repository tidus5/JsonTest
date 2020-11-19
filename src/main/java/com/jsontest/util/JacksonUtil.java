package com.jsontest.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.jsontest.bean.JsonBean;

import java.io.IOException;
import java.sql.Time;

public class JacksonUtil {

    public static ObjectMapper objectMapper = initJackson();


    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static ObjectMapper initJackson(){
        ObjectMapper mapper = new ObjectMapper();
        // 禁用遇到空对象就失败的特性
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // 序列化不包含 null 值
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //对map的key不要求包含引号 (兼容fastjson设置）
        mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        // jackson默认开启遇到未知属性需要抛异常，因此如要和fastjson保持一致则需要关闭该特性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        //序列化时忽略transient修饰的field (在有getter setter时才会起作用。  如果没有getter setter， 不会序列化transient字段）
        mapper.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);


        SimpleModule module = new SimpleModule();
        module.addSerializer(java.sql.Time.class, new SqlTimeJacksonSerializer());
        module.addDeserializer(java.sql.Time.class, new SqlTimeJacksonDeserializer());
        mapper.registerModule(module);

        //禁用 AUTOTYPE   AUTO Type容易导致安全漏洞，强烈建议禁用
        mapper.deactivateDefaultTyping();

        return mapper;
    }


    public static String writeValueAsString(JsonBean bean) {
        try {
            return getObjectMapper().writeValueAsString(bean);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readValue(String json, Class<T> valueType) {
        try {
            return getObjectMapper().readValue(json, valueType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // jackson 对 sql.time 的序列化和反序列化
    static class SqlTimeJacksonSerializer extends com.fasterxml.jackson.databind.JsonSerializer<java.sql.Time> {
        @Override
        public void serialize(java.sql.Time value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            long ts = value.getTime();
            gen.writeNumber(ts);
        }
    }
    static class SqlTimeJacksonDeserializer extends com.fasterxml.jackson.databind.JsonDeserializer<java.sql.Time> {
        @Override
        public Time deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            long ts = p.getLongValue();
            java.sql.Time time = new java.sql.Time(ts);
            return time;
        }
    }
}
