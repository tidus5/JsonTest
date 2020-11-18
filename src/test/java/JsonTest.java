

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.internal.bind.ObjectTypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.sql.Time;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class JsonTest {

    //Jackson替换fastjson 相关资料
    // https://www.cnblogs.com/larva-zhh/p/11544317.html

    private static int fastJsonSerFeature = JSON.DEFAULT_GENERATE_FEATURE;   //toJsonString 用的
    private static int fastJsonDeserFeature = JSON.DEFAULT_PARSER_FEATURE;   //parseObject 用的
    public Gson gson;
    public ObjectMapper objectMapper;

    public JsonTest() {
        init();
    }

    public void init(){
        initFastJson();
        gson = initGson();
        objectMapper = initJackson();
    }

    public static void initFastJson() {
        // map的字段按字母顺序排序
        fastJsonSerFeature |= SerializerFeature.MapSortField.getMask();
        //关闭fastjson的按字母排序的默认特性，改为按字段定义顺序 (gson 和 jackson 都是默认按字段定义顺序）
        fastJsonSerFeature &= ~SerializerFeature.SortField.getMask();

        // 非字符串的key加上引号
        fastJsonSerFeature |= SerializerFeature.WriteNonStringKeyAsString.getMask();
        //  关闭循环引用检测 （重复引用 不使用 $ref， 但循环引用会抛异常)
        fastJsonSerFeature |= SerializerFeature.DisableCircularReferenceDetect.getMask();



        //不输出为null 的字段
        fastJsonSerFeature &= ~SerializerFeature.WriteMapNullValue.getMask();


        //////////////////////////////////////////////////////////////////////////////////
//        fastJsonDeserFeature &= ~Feature.SortFeidFastMatch.getMask();

        //  将json中的浮点数解析成BigDecimal对象，禁用后会解析成Double对象
        fastJsonDeserFeature &= ~Feature.UseBigDecimal.getMask();



        //序列化时忽略transient修饰的field
//        fastJsonFeature |= SerializerFeature.SkipTransientField.getMask();
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

        return mapper;
    }

    public Gson getGson() {
        return gson;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static void main(String[] args) throws JsonProcessingException {
        JsonTest test = new JsonTest();
//        test.testSerialize();
//        test.testDeserialize();
//        test.testDeserialize2();

        JUnitCore.runClasses(new Class[] {JsonTest.class });
    }

    @Test
    public void testSerialize() {
        JsonBean bean = JsonBean.getTestBean();

        String fastjsonStr = JSON.toJSONString(bean, fastJsonSerFeature);
        String gsonStr = getGson().toJson(bean);
        String jacksonStr = "";
        try {
            jacksonStr = getObjectMapper().writeValueAsString(bean);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        System.out.println();
        System.out.println("testSerialize:");
        System.out.println("Fastjson:" + fastjsonStr);
        System.out.println("Gson:    " + gsonStr);
        System.out.println("Jackson: " + jacksonStr);

        System.err.println("fastjson == gson: \t\t" + fastjsonStr.equals(gsonStr));
        System.err.println("fastjson == jackson:\t" + fastjsonStr.equals(jacksonStr));

        assert fastjsonStr.equals(gsonStr);
        assert fastjsonStr.equals(jacksonStr);

    }

    @Test
    public void testDeserialize() {
        JsonBean bean = JsonBean.getTestBean();
        String json = JSON.toJSONString(bean, fastJsonSerFeature);

        JsonBean fastjsonObj = JSON.parseObject(json, JsonBean.class, fastJsonDeserFeature);
        JsonBean gsonObj = getGson().fromJson(json, JsonBean.class);
        JsonBean jacksonObj = null;
        try {
            jacksonObj = getObjectMapper().readValue(json, JsonBean.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println();
        System.out.println("test Deserialize:");
        System.out.println("original:" + json);
        System.out.println("Fastjson:" + JSON.toJSONString(fastjsonObj, fastJsonSerFeature));
        System.out.println("Gson:    " + JSON.toJSONString(gsonObj, fastJsonSerFeature));
        System.out.println("Jackson: " + JSON.toJSONString(jacksonObj, fastJsonSerFeature));

        System.err.println("FastjsonBean == GsonBean: \t\t" + fastjsonObj.equals(gsonObj));
        System.err.println("FastjsonBean == JacksonBean:\t" + fastjsonObj.equals(jacksonObj));

        assert fastjsonObj.equals(gsonObj);
        assert fastjsonObj.equals(jacksonObj);

        assert ((Map)fastjsonObj.mapObject).get("double").getClass() == java.lang.Double.class;
        assert ((Map)fastjsonObj.mapObject).get("double").getClass() == java.lang.Double.class;
        assert ((Map)fastjsonObj.mapObject).get("double").getClass() == java.lang.Double.class;


//        testDeserialize2();
    }

//    @Test
    public void testDeserialize2() {
        String json = "{\"anInt\": 1, \"abInt\": 2}";
        JsonBean fastjsonObj = JSON.parseObject(json, JsonBean.class, fastJsonDeserFeature);
        JsonBean gsonObj = getGson().fromJson(json, JsonBean.class);
        JsonBean jacksonObj = null;
        try {
            jacksonObj = getObjectMapper().readValue(json, JsonBean.class);
        } catch (IOException e) {
            e.printStackTrace();
        }


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
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new java.util.Date(json.getAsJsonPrimitive().getAsLong());
        }
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getTime());
        }
    }

    static class SqlDateToLongTypeAdapter implements JsonDeserializer<java.sql.Date>, JsonSerializer<java.sql.Date> {
        public java.sql.Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new java.sql.Date(json.getAsJsonPrimitive().getAsLong());
        }
        public JsonElement serialize(java.sql.Date src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getTime());
        }
    }

    static class SqlTimeToLongTypeAdapter implements JsonDeserializer<java.sql.Time>, JsonSerializer<java.sql.Time> {
        public java.sql.Time deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new java.sql.Time(json.getAsJsonPrimitive().getAsLong());
        }
        public JsonElement serialize(java.sql.Time src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getTime());
        }
    }

    static class CalendarToLongTypeAdapter implements JsonDeserializer<Calendar>, JsonSerializer<Calendar> {
        public Calendar deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(json.getAsJsonPrimitive().getAsLong());
            return calendar;
        }
        public JsonElement serialize(Calendar src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getTimeInMillis());
        }
    }

    static class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
        public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Base64.decodeBase64(json.getAsString());
        }

        public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(Base64.encodeBase64String(src));
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

    static class JsonBean {
        public Date date = new Date();
        public byte aByte = (byte) 33;
        public short aShort = (short) -2;
        public int anInt = -3;
        public Integer integer = -4;
        public long aLong = -5;
        public Long aLong2 = -6L;
        public boolean aBoolean = true;
        public Boolean aBoolean2 = true;

        public float aFloat = 1.2f;
        public Float aFloat2 = 3.4f;
        public double aDouble = 5.6;
        public Double aDouble2 = 7.8;
        public char aChar = 'z';
        public Character character = 'p';

        public String string = "中文rrにちもん!$^&*(@%";

        public int[] intArray = new int[]{3, 4, 6};
        public float[] floatArray = new float[]{2.3f, 4.5f};
        public double[] doubleArray = new double[]{5.666, 8.99};
        public String[] stringArray = new String[]{"add=+$#", "oor"};
        public byte[] byteArray = new byte[]{(byte) 4, (byte) 7};
        public Object[] objectsArray = new Object[]{date, 9, 3.5f, 6.7, null, new Object()};

        public List<Integer> integerList = Lists.newArrayList(1, 6, 8);
        public List<Float> floatList = Lists.newLinkedList(Arrays.asList(4.5f, 6.7f));
        public List<Double> doubleList = Lists.newArrayList(4.7777);
        public List<String> stringList = Lists.newArrayList("ffff");
        public List<Object> objectList = Lists.newArrayList(new Object(), new int[]{2, 3});

        //set 序列化是无序的，这里只测试有序的
//        public Set<Integer> integerSet = Sets.newHashSet(4, 5, 9);
        public Map<String, Integer> stringIntegerMap = Maps.newTreeMap();

        public Object nullObject = null;
        public Object intObject = 1;
        public Object longObject = 2L;
        public Object mapObject = new TreeMap<>();

        public AtomicInteger atomicInteger = new AtomicInteger(100);
        public AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        public AtomicLong atomicLong = new AtomicLong(358L);
        public AtomicLong[] atomicLongArray = new AtomicLong[]{new AtomicLong(13), new AtomicLong(15)};
        public AtomicInteger[] atomicIntegerArray = new AtomicInteger[]{new AtomicInteger(133), new AtomicInteger(125)};

        public StringBuilder stringBuilder = new StringBuilder().append("test");
        public StringBuffer stringBuffer = new StringBuffer().append(1).append(true);

        public BigDecimal bigDecimal = BigDecimal.valueOf(999888777666555444333222111.123456789);
        public BigInteger bigInteger = BigInteger.valueOf(Long.MAX_VALUE);
        public URL url;
        public URI uri;
        public UUID uuid = UUID.fromString("1b062d76-4c6d-434a-a32e-c954176eb960");
        public Currency currency = Currency.getInstance(Locale.CHINA);
        public Locale locale = Locale.CANADA;
        public InetAddress inetAddress;

        //bitset 在 fastjson 和 jackson都无法正确序列化。
//        public BitSet bitSet = BitSet.valueOf(new byte[]{(byte)128,(byte)2});

        public Calendar calendar = Calendar.getInstance();
        public java.sql.Time sqlTime = new java.sql.Time(date.getTime());
        public java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        public java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(date.getTime());
        public Locale.Category oneEnum = Locale.Category.DISPLAY;

        public transient int testTransient = 1;

        public int getTestTransient() {
            return testTransient;
        }

        public void setTestTransient(int testTransient) {
            this.testTransient = testTransient;
        }

        public static JsonBean getTestBean() {
            JsonBean bean = new JsonBean();
            Map m = Maps.newTreeMap();
            m.put("oo", 1);
            m.put("ee", 2.3f);
            bean.objectList.add(m);
            bean.objectList.add(m); //测试重复引用
            bean.objectList.add(null);
            bean.objectList.add(null);

            //循环引用在gson 和 jackson都会报错。。
            //fastjson默认开启循环引用检测，但开启后重复引用会使用$ref, 和其他json格式会有差异
//            m.put("ddf", bean.objectList);

            bean.stringIntegerMap.put("a", 33);
            bean.stringIntegerMap.put("d", 99);
            bean.stringIntegerMap.put("b", 66);
            bean.stringIntegerMap.put("c", 77);

            ((TreeMap) bean.mapObject).put("r", 2);
            ((TreeMap) bean.mapObject).put("1", "ok");
            ((TreeMap) bean.mapObject).put("double", 1.2);

            try {
                bean.url = new URL("http://www.bing.com");
                bean.uri = new URI("file:///d:/test/test.txt");
//                bean.inetAddress = InetAddress.getLocalHost();    //在linux 不可靠
                bean.inetAddress = getLocalHostExactAddress();
            } catch (MalformedURLException | URISyntaxException e) {
                e.printStackTrace();
            }

            bean.calendar.setTimeInMillis(bean.date.getTime());

            return bean;
        }

        @Override
        public boolean equals(Object obj) {
            String selfFastjsonStr = JSON.toJSONString(this, fastJsonSerFeature);
            String objFastjsonStr = JSON.toJSONString(obj, fastJsonSerFeature);
            return selfFastjsonStr.equals(objFastjsonStr);
        }

        @Override
        public String toString() {
            return JSON.toJSONString(this,fastJsonSerFeature);
        }
    }

    public static InetAddress getLocalHostExactAddress() {
        try {
            InetAddress candidateAddress = null;

            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface iface = networkInterfaces.nextElement();
                // 该网卡接口下的ip会有多个，也需要一个个的遍历，找到自己所需要的
                for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = inetAddrs.nextElement();
                    // 排除loopback回环类型地址（不管是IPv4还是IPv6 只要是回环地址都会返回true）
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了 就是我们要找的
                            // ~~~~~~~~~~~~~绝大部分情况下都会在此处返回你的ip地址值~~~~~~~~~~~~~
                            return inetAddr;
                        }

                        // 若不是site-local地址 那就记录下该地址当作候选
                        if (candidateAddress == null) {
                            candidateAddress = inetAddr;
                        }

                    }
                }
            }

            // 如果出去loopback回环地之外无其它地址了，那就回退到原始方案吧
            return candidateAddress == null ? InetAddress.getLocalHost() : candidateAddress;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
