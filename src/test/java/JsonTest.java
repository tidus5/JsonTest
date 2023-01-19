import com.alibaba.fastjson.parser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.annotations.SerializedName;
import com.jsontest.bean.BaseJsonBean;
import com.jsontest.bean.JsonBean;
import com.jsontest.bean.JsonBeanWithTree;
import com.jsontest.bean.SimpleJsonBean;
import com.jsontest.util.FastJsonUtil;
import com.jsontest.util.GsonUtil;
import com.jsontest.util.JacksonUtil;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JsonTest {

    //Jackson替换fastjson 相关资料
    // https://www.cnblogs.com/larva-zhh/p/11544317.html


    public static boolean printDetail = false;

    public JsonTest() {
        init();
    }

    public void init(){
//        FastJsonUtil.initFastJson();
//        GsonUtil.initGson();
//        JacksonUtil.initJackson();
    }

    public static void main(String[] args) throws JsonProcessingException, NoSuchFieldException {
        JsonTest test = new JsonTest();
//        test.testSerialize();
//        test.testDeserialize();
//        test.testDeserialize2();
        JsonBean fastjsonObj = FastJsonUtil.parseObject("", JsonBean.class);
        System.out.println(fastjsonObj);

//        test.testDeserialize("");
        JsonBean jacksonObj = JacksonUtil.readValue("{}", JsonBean.class);

        jacksonObj = JacksonUtil.readValue("{\"jacksonIgnoreField\":3, \"testTransient\":34}", JsonBean.class);

//        JUnitCore.runClasses(new Class[] {JsonTest.class });

        SimpleJsonBean ee = new SimpleJsonBean();
        ee.timeUnit = TimeUnit.DAYS;

        System.out.println(GsonUtil.toJson(ee));

        System.out.println(ee.timeUnit.getClass());
        System.out.println(ee.timeUnit.getDeclaringClass());
        Enum item = ee.timeUnit;
        System.out.println(item.getDeclaringClass());
        String name = item.name();
        System.out.println(name);
        System.out.println(item.getDeclaringClass().getField(name).getAnnotation(SerializedName.class));
    }

    @Test
    public void testSerialize() {
        JsonBean bean = JsonBean.getTestBean();

        String gsonStr = GsonUtil.toJson(bean);
        String fastjsonStr = FastJsonUtil.toJSONString(bean);
        String jacksonStr = JacksonUtil.writeValueAsString(bean);

        printDetail = true;
        if(printDetail) {
            System.out.println();
            System.out.println("testSerialize:");
            System.out.println("Fastjson:" + fastjsonStr);
            System.out.println("Gson:    " + gsonStr);
            System.out.println("Jackson: " + jacksonStr);

            System.err.println("jackson  == gson: \t\t" + jacksonStr.equals(gsonStr));
            System.err.println("fastjson == gson: \t\t" + fastjsonStr.equals(gsonStr));
            System.err.println("fastjson == jackson:\t" + fastjsonStr.equals(jacksonStr));
        }

//
//        SimpleJsonBean sjb = new SimpleJsonBean();
//        sjb.timeUnit = TimeUnit.DAYS;
//        String rrs = GsonUtil.toJson(sjb);
//        System.out.println(rrs);

        //https://github.com/alibaba/fastjson/issues/3115
        // 由于序列化实现不同，这里bean在没有getter setter时，可以让fastjson 与其他输出相同
        assert gsonStr.equals(jacksonStr);
        assert fastjsonStr.equals(gsonStr);
        assert fastjsonStr.equals(jacksonStr);



    }

    @Test
    public void testDeserialize() {
        JsonBean bean = JsonBean.getTestBean();
        String json = FastJsonUtil.toJSONString(bean);
        testJsonBeanDeserialize(json);
    }


    /**
     * 如果有父类，jackson会先取父类字段，fastjson和gson 最后取父类字段。所以序列化会不一致
     * 而gson默认不大支持指定序列化顺序。
     */
    public void testDeserialize(String json, Class<? extends BaseJsonBean> clz) {
        BaseJsonBean gsonObj = GsonUtil.fromJson(json, clz);
        BaseJsonBean fastjsonObj = FastJsonUtil.parseObject(json, clz);
        BaseJsonBean jacksonObj = JacksonUtil.readValue(json, clz);

        boolean fastJacksonEqual = fastjsonObj == null ? (jacksonObj == null) : (fastjsonObj.equals(jacksonObj));
        boolean fastGsonEqual = fastjsonObj == null ? (gsonObj == null) :fastjsonObj.equals(gsonObj);
        boolean jacksonGsonEqual = jacksonObj == null ? (gsonObj == null) : jacksonObj.equals(gsonObj);

        if(printDetail) {
            System.out.println();
            System.out.println("test Deserialize:");
            System.out.println("original:" + json);
            System.out.println("Fastjson:" + fastjsonObj);
            System.out.println("Gson:    " + gsonObj);
            System.out.println("Jackson: " + jacksonObj);

            System.err.println("FastjsonBean == JacksonBean:\t" + fastJacksonEqual);
            System.err.println("FastjsonBean == GsonBean: \t\t" + fastGsonEqual);
            System.err.println("JacksonBean  == GsonBean: \t\t" + jacksonGsonEqual);

        }

        assert fastJacksonEqual;
        assert fastGsonEqual;
        assert jacksonGsonEqual;

        if (fastjsonObj != null && fastjsonObj.mapObject != null
                && ((Map) fastjsonObj.mapObject).get("double") != null) {
            assert ((Map) fastjsonObj.mapObject).get("double").getClass() == java.lang.Double.class;
            assert ((Map) gsonObj.mapObject).get("double").getClass() == java.lang.Double.class;
            assert ((Map) jacksonObj.mapObject).get("double").getClass() == java.lang.Double.class;
        }
    }


    public void testJsonBeanDeserialize(String json) {
        JsonBean gsonObj = GsonUtil.fromJson(json, JsonBean.class);
        JsonBean fastjsonObj = FastJsonUtil.parseObject(json, JsonBean.class);
        JsonBean jacksonObj = JacksonUtil.readValue(json, JsonBean.class);

        if(printDetail) {
            System.out.println();
            System.out.println("test Deserialize:");
            System.out.println("original:" + json);
            System.out.println("Fastjson:" + fastjsonObj);
            System.out.println("Gson:    " + gsonObj);
            System.out.println("Jackson: " + jacksonObj);

            System.err.println("FastjsonBean == GsonBean: \t\t" + fastjsonObj.equals(gsonObj));
            System.err.println("FastjsonBean == JacksonBean:\t" + fastjsonObj.equals(jacksonObj));
        }

        assert fastjsonObj == null ? (jacksonObj == null) : (fastjsonObj.equals(jacksonObj));
        assert fastjsonObj == null ? (gsonObj == null) :fastjsonObj.equals(gsonObj);
        assert jacksonObj == null ? (gsonObj == null) : jacksonObj.equals(gsonObj);

        if (fastjsonObj != null && fastjsonObj.mapObject != null
                && ((Map) fastjsonObj.mapObject).get("double") != null) {
            assert ((Map) fastjsonObj.mapObject).get("double").getClass() == java.lang.Double.class;
            assert ((Map) gsonObj.mapObject).get("double").getClass() == java.lang.Double.class;
            assert ((Map) jacksonObj.mapObject).get("double").getClass() == java.lang.Double.class;
        }
    }

    @Test
    public void testNotExistFieldDeserialize() {

        String json = "{\"anInt\": 1, \"notExistInt\": 2, \"ttt\":233}";
        testJsonBeanDeserialize(json);
        testJsonBeanDeserialize("{\"name\":123,\"age\":345}");

        testDeserialize("{\"jacksonIgnoreField\":3, \"testTransient\":34}",  SimpleJsonBean.class);
        testDeserialize("{'testInt':34, testString:null}",  SimpleJsonBean.class);

        testDeserialize("{'testInt':001, testString:null}",  SimpleJsonBean.class);

        // enum 用下标
        testDeserialize("{'testInt':null, 'timeUnit':2}",  SimpleJsonBean.class);
        //  enum 不存在
        testDeserialize("{'testInt':null, 'timeUnit':'DAYS_ERROR'}",  SimpleJsonBean.class);

        testDeserialize("",  SimpleJsonBean.class);
    }

    @Test
    public void testOrderdArrayDeserialize() {

        String json = "[{\"test\": 0}, {\"test\": 1}]";
        List<Map> list = FastJsonUtil.parseObject(json, List.class, Feature.OrderedField);
        assert (int) list.get(0).get("test") == 0;
        assert (int) list.get(1).get("test") == 1;
        list = GsonUtil.getGson().fromJson(json, List.class);
        assert (int) list.get(0).get("test") == 0;
        assert (int) list.get(1).get("test") == 1;
        list = JacksonUtil.readValue(json, List.class);
        assert (int) list.get(0).get("test") == 0;
        assert (int) list.get(1).get("test") == 1;


        json = "[{\"test\": 1}, {\"test\": 0}]";
        list = FastJsonUtil.parseObject(json, List.class, Feature.OrderedField);
        assert (int) list.get(0).get("test") == 1;
        assert (int) list.get(1).get("test") == 0;
        list = GsonUtil.getGson().fromJson(json, List.class);
        assert (int) list.get(0).get("test") == 1;
        assert (int) list.get(1).get("test") == 0;
        list = JacksonUtil.readValue(json, List.class);
        assert (int) list.get(0).get("test") == 1;
        assert (int) list.get(1).get("test") == 0;
    }




    @Test
    public void testDeserializeTreeMapAndSet() {
//        JsonBean bean = JsonBean.getTestBean();
//        String json = FastJsonUtil.toJSONString(bean);
//        String json = "{\"integerSet\":[4,1,2,6,41,5,9,88,31,45,15],\"stringIntegerMap\":{\"b\":66,\"c\":77,\"a\":33,\"z\":4}, \"sb\":\"123test\"}";
        String json = "{ \"sb\":\"123test\"}";
        testDeserializeTreeMapAndSet(json);

    }

    public void testDeserializeTreeMapAndSet(String json) {
        JsonBeanWithTree gsonObj = GsonUtil.fromJson(json, JsonBeanWithTree.class);
        JsonBeanWithTree fastjsonObj = FastJsonUtil.parseObject(json, JsonBeanWithTree.class);
        JsonBeanWithTree jacksonObj = JacksonUtil.readValue(json, JsonBeanWithTree.class);

        if(printDetail) {
            System.out.println();
            System.out.println("test Deserialize:");
            System.out.println("original:" + json);
            System.out.println("Fastjson:" + fastjsonObj.toString());
            System.out.println("Gson:    " + gsonObj.toString());
            System.out.println("Jackson: " + jacksonObj.toString());

            System.err.println("FastjsonBean == GsonBean: \t\t" + fastjsonObj.equals(gsonObj));
            System.err.println("FastjsonBean == JacksonBean:\t" + fastjsonObj.equals(jacksonObj));
        }

        assert fastjsonObj.equals(jacksonObj);
        assert fastjsonObj.equals(gsonObj);
        assert jacksonObj.equals(gsonObj);

    }

}
