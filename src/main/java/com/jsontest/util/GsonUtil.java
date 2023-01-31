package com.jsontest.util;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.internal.bind.ObjectTypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.*;

/**
 * gson 对字段排除， 字段指定顺序的支持比较麻烦
 *
 * Google Gson用法详解：
 * https://developer.aliyun.com/article/840439

 */
public class GsonUtil {

    public static Gson gson;

    public static Gson getGson() {
        if (gson == null) {
            synchronized (GsonUtil.class) {
                if (gson == null) {
                    gson = initGson();
                }
            }
        }
        return gson;
    }

    public static Gson initGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        // 指定gson对Date序列化为long时间戳，（与fastjson，jackson保持兼容）
        gsonBuilder.registerTypeAdapter(java.util.Date.class, new DateToLongTypeAdapter()).setDateFormat(DateFormat.LONG);
        gsonBuilder.registerTypeAdapter(java.sql.Date.class, new SqlDateToLongTypeAdapter()).setDateFormat(DateFormat.LONG);
        gsonBuilder.registerTypeAdapter(java.sql.Time.class, new SqlTimeToLongTypeAdapter()).setDateFormat(DateFormat.LONG);
        gsonBuilder.registerTypeHierarchyAdapter(java.util.Calendar.class, new CalendarToLongTypeAdapter()).setDateFormat(DateFormat.LONG);
        // 指定gson 对byte[] 序列化后进行base64编码，（与fastjson，jackson保持兼容）
        gsonBuilder.registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter());
        gsonBuilder.registerTypeHierarchyAdapter(Enum.class, new GsonEnumByOrdinalSerializer());
        // 指定gson不需要对 = 等特殊字符进行html转义 （与fastjson，jackson保持兼容）
        gsonBuilder.disableHtmlEscaping();
        Gson newGson = gsonBuilder.create();
        // 反射替换gson的 ObjectTypeAdapter，使其支持整数和小数的区分
        try {
            Field factories = Gson.class.getDeclaredField("factories");
            factories.setAccessible(true);
            Object o = factories.get(newGson);
            Class<?>[] declaredClasses = Collections.class.getDeclaredClasses();
            for (Class c : declaredClasses) {
                if ("java.util.Collections$UnmodifiableList".equals(c.getName())) {
                    Field listField = c.getDeclaredField("list");
                    listField.setAccessible(true);
                    List<TypeAdapterFactory> list = (List<TypeAdapterFactory>) listField.get(o);
                    int i = list.indexOf(ObjectTypeAdapter.FACTORY);
                    list.set(i, CustomObjectTypeAdapter.FACTORY);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newGson;
    }

    public static String toJson(Object bean) {
        return getGson().toJson(bean);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return getGson().fromJson(json, classOfT);
    }


    static class CustomObjectTypeAdapter extends TypeAdapter<Object> {
        public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                if (type.getRawType() == Object.class) {
                    return (TypeAdapter<T>) new CustomObjectTypeAdapter(gson);
                }
                return null;
            }
        };

        private final Gson gson;

        private CustomObjectTypeAdapter(Gson gson) {
            this.gson = gson;
        }

        @Override
        public Object read(JsonReader in) throws IOException {
            JsonToken token = in.peek();
            //判断字符串的实际类型
            switch (token) {
                case BEGIN_ARRAY:
                    List<Object> list = new ArrayList<>();
                    in.beginArray();
                    while (in.hasNext()) {
                        list.add(read(in));
                    }
                    in.endArray();
                    return list;

                case BEGIN_OBJECT:
                    Map<String, Object> map = new LinkedTreeMap<>();
                    in.beginObject();
                    while (in.hasNext()) {
                        map.put(in.nextName(), read(in));
                    }
                    in.endObject();
                    return map;
                case STRING:
                    return in.nextString();
                case NUMBER:
                    String s = in.nextString();
                    if (s.contains(".")) {
                        return Double.valueOf(s);
                    } else {
                        try {
                            return Integer.valueOf(s);
                        } catch (Exception e) {
                            return Long.valueOf(s);
                        }
                    }
                case BOOLEAN:
                    return in.nextBoolean();
                case NULL:
                    in.nextNull();
                    return null;
                default:
                    throw new IllegalStateException();
            }
        }

        @Override
        public void write(JsonWriter out, Object value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            //noinspection unchecked
            TypeAdapter<Object> typeAdapter = (TypeAdapter<Object>) gson.getAdapter(value.getClass());
            if (typeAdapter instanceof CustomObjectTypeAdapter) {
                out.beginObject();
                out.endObject();
                return;
            }
            typeAdapter.write(out, value);
        }
    }

    static class DateToLongTypeAdapter implements JsonDeserializer<Date>, JsonSerializer<Date> {
        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new java.util.Date(json.getAsJsonPrimitive().getAsLong());
        }
        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getTime());
        }
    }

    static class SqlDateToLongTypeAdapter implements JsonDeserializer<java.sql.Date>, JsonSerializer<java.sql.Date> {
        @Override
        public java.sql.Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new java.sql.Date(json.getAsJsonPrimitive().getAsLong());
        }
        @Override
        public JsonElement serialize(java.sql.Date src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getTime());
        }
    }

    static class SqlTimeToLongTypeAdapter implements JsonDeserializer<java.sql.Time>, JsonSerializer<java.sql.Time> {
        @Override
        public java.sql.Time deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new java.sql.Time(json.getAsJsonPrimitive().getAsLong());
        }
        @Override
        public JsonElement serialize(java.sql.Time src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getTime());
        }
    }

    static class CalendarToLongTypeAdapter implements JsonDeserializer<Calendar>, JsonSerializer<Calendar> {
        @Override
        public Calendar deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(json.getAsJsonPrimitive().getAsLong());
            return calendar;
        }
        @Override
        public JsonElement serialize(Calendar src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getTimeInMillis());
        }
    }

    static class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
        @Override
        public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return org.apache.commons.codec.binary.Base64.decodeBase64(json.getAsString());
        }

        @Override
        public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(Base64.encodeBase64String(src));
        }
    }

    /**
     * https://springboot.io/t/topic/1737
     * @param <T>
     */
    static class GsonEnumByOrdinalSerializer<T extends Enum> implements JsonSerializer<T>, JsonDeserializer<T> {

        @Override
        public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                // 必须是基本数据类型
                if (json.isJsonPrimitive()) {
                    JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive();
                    // 反射读取所有得枚举实例
                    T[] enumConstants  = (T[]) Class.forName(((Class)typeOfT).getName()).getEnumConstants();

                    if (jsonPrimitive.isNumber()) { // 数字
                        return enumConstants[jsonPrimitive.getAsInt()];
                    } else if (jsonPrimitive.isString()) { // 字符串
                        String val = jsonPrimitive.getAsString();
                        for (T constant : enumConstants) {
                            if (constant.name().equalsIgnoreCase(val)) {
                                return constant;
                            }
                        }
                        return null;
                    }
                }
            } catch (ClassNotFoundException | ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            throw new IllegalArgumentException("bad param:" + json.getAsString());
        }

        @Override
        public JsonElement serialize(T item, Type type, JsonSerializationContext jsonSerializationContext) {
            return item == null ? JsonNull.INSTANCE : new JsonPrimitive(item.name());
        }
    }

    //  gson 默认enum 处理类（作为参考）
    private static final class EnumTypeAdapter<T extends Enum<T>> extends TypeAdapter<T> {
        private final Map<String, T> nameToConstant = new HashMap<String, T>();
        private final Map<T, String> constantToName = new HashMap<T, String>();

        public EnumTypeAdapter(Class<T> classOfT) {
            try {
                for (T constant : classOfT.getEnumConstants()) {
                    String name = constant.name();
                    SerializedName annotation = classOfT.getField(name).getAnnotation(SerializedName.class);
                    if (annotation != null) {
                        name = annotation.value();
                        for (String alternate : annotation.alternate()) {
                            nameToConstant.put(alternate, constant);
                        }
                    }
                    nameToConstant.put(name, constant);
                    constantToName.put(constant, name);
                }
            } catch (NoSuchFieldException e) {
                throw new AssertionError(e);
            }
        }
        @Override public T read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return nameToConstant.get(in.nextString());
        }

        @Override public void write(JsonWriter out, T value) throws IOException {
            out.value(value == null ? null : constantToName.get(value));
        }
    }
}
