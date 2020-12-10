package com.jsontest.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.sql.Time;

public class JacksonUtil {

    public static ObjectMapper objectMapper = initJackson();


    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * https://www.cnblogs.com/larva-zhh/p/11544317.html
     * @return
     */
    public static ObjectMapper initJackson(){
        JsonMapper.Builder builder = JsonMapper.builder();

        //关闭遇到空对象就失败的特性
        builder.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        //开启允许filed没有引号 (与fastjson保持兼容）
        builder.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        //关闭遇到未知属性抛异常(与fastjson保持兼容）
        builder.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //开启序列化时忽略transient修饰的field (与fastjson保持兼容。在有getter setter时才会起作用。如果没有getter setter时，不会序列化transient字段）
        builder.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);

        ObjectMapper mapper = builder.build();
        //序列化不包含 null值 (与fastjson保持兼容）
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        SimpleModule module = new SimpleModule();
        module.addSerializer(java.sql.Time.class, new SqlTimeJacksonSerializer());
        module.addDeserializer(java.sql.Time.class, new SqlTimeJacksonDeserializer());
        mapper.registerModule(module);

        //jackson的PolymorphicDeserialization默认是支持Object.class、abstract classes、interfaces属性的AUTO Type，
        // 但是该特性容易导致安全漏洞，强烈建议使用ObjectMapper.disableDefaultTyping()设置为只允许@JsonTypeInfo生效
        //https://www.cnblogs.com/larva-zhh/p/11544317.html
        mapper.deactivateDefaultTyping();

        return mapper;
    }


    public static String writeValueAsString(Object bean) {
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
