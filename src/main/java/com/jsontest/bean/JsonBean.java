package com.jsontest.bean;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jsontest.util.NetUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.jsontest.util.FastJsonUtil.SerializeFeature;

public class JsonBean {
    private static Date date = new Date();

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
    public String[] stringArray = new String[]{"add=+$#", "oor", null, ""};
    public byte[] byteArray = new byte[]{(byte) 4, (byte) 7};
    public Object[] objectsArray = new Object[]{date, 9, 3.5f, 6.7, null, new Object()};
    public Object[] nullArray = null;

    public List<Integer> integerList = Lists.newArrayList(1, 6, 8, null, 10);
    public List<Float> floatList = Lists.newLinkedList(Arrays.asList(4.5f, 6.7f));
    public List<Double> doubleList = Lists.newArrayList(4.7777);
    public List<String> stringList = Lists.newArrayList("ffff");
    public List<Object> objectList = Lists.newArrayList(new Object(), new int[]{2, 3});
    public List<Object> nullList = null;

    public TreeSet<Integer> integerSet = Sets.newTreeSet();
    public TreeMap<String, Integer> stringIntegerMap = Maps.newTreeMap();

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
    public Date aDate = new Date();
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

    @JsonIgnore
    public int getIsOpen() {
        return 0;
    }

    public JsonBean() {
        calendar.setTimeInMillis(date.getTime());
        aDate.setTime(date.getTime());
        Map m = Maps.newTreeMap();
        m.put("oo", 1);
        m.put("ee", 2.3f);
        m.put("ff", null);
        objectList.add(m);
        objectList.add(m); //测试重复引用
        objectList.add(null);
        objectList.add(null);

        //循环引用在gson 和 jackson都会报错。。
        //fastjson默认开启循环引用检测，但开启后重复引用会使用$ref, 和其他json格式会有差异
//            m.put("ddf", bean.objectList);

        stringIntegerMap.put("a", 33);
        stringIntegerMap.put("d", 99);
        stringIntegerMap.put("b", 66);
        stringIntegerMap.put("z", 4);
        stringIntegerMap.put("c", 77);
        stringIntegerMap.put("d", null);

        integerSet.addAll(Sets.newHashSet(4, 15, 9, 45, 1, 6, 88, 31));

        ((TreeMap) mapObject).put("r", 2);
        ((TreeMap) mapObject).put("1", "ok");
        ((TreeMap) mapObject).put("double", 1.2);

        try {
            url = new URL("http://www.bing.com");
            uri = new URI("file:///d:/test/test.txt");
//                bean.inetAddress = InetAddress.getLocalHost();    //在linux 不可靠
            inetAddress = NetUtil.getLocalHostExactAddress();
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static JsonBean getTestBean() {
        JsonBean bean = new JsonBean();
        return bean;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof JsonBean) {
            return obj.toString().equals(this.toString());
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        //https://www.cnblogs.com/larva-zhh/p/11544317.html
        //map的字段按字母顺序排序。 默认是关闭的（建议关闭，开启会影响性能）
        //输出字符串时，开启map排序。这里开启为了统一输出格式

//        bean在没有getter setter时，可以让fastjson 序列化时与其他输出相同，大概率按声明顺序，方便比较序列化结果。
//        https://github.com/alibaba/fastjson/issues/3115
        return JSON.toJSONString(this, SerializeFeature, SerializerFeature.MapSortField);
    }
}
