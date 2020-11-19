package com.jsontest.bean;



import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jsontest.util.NetUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.jsontest.util.FastJsonUtil.SerializeFeature;

public class JsonBean {
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
                bean.inetAddress = NetUtil.getLocalHostExactAddress();
            } catch (MalformedURLException | URISyntaxException e) {
                e.printStackTrace();
            }

            bean.calendar.setTimeInMillis(bean.date.getTime());

            return bean;
        }

        @Override
        public boolean equals(Object obj) {
            String selfFastjsonStr = JSON.toJSONString(this, SerializeFeature);
            String objFastjsonStr = JSON.toJSONString(obj, SerializeFeature);
            return selfFastjsonStr.equals(objFastjsonStr);
        }

        @Override
        public String toString() {
            return JSON.toJSONString(this, SerializeFeature);
        }
    }
