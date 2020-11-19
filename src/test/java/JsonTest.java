import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.jsontest.bean.JsonBean;
import com.jsontest.util.FastJsonUtil;
import com.jsontest.util.GsonUtil;
import com.jsontest.util.JacksonUtil;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import java.util.HashMap;
import java.util.Map;

public class JsonTest {

    //Jackson替换fastjson 相关资料
    // https://www.cnblogs.com/larva-zhh/p/11544317.html


    public JsonTest() {
        init();
    }

    public void init(){
//        FastJsonUtil.initFastJson();
//        GsonUtil.initGson();
//        JacksonUtil.initJackson();
    }

    public static void main(String[] args) throws JsonProcessingException {
        JsonTest test = new JsonTest();
//        test.testSerialize();
//        test.testDeserialize();
//        test.testDeserialize2();


//        JUnitCore.runClasses(new Class[] {JsonTest.class });
    }

    @Test
    public void testSerialize() {
        JsonBean bean = JsonBean.getTestBean();

        String gsonStr = GsonUtil.toJson(bean);
        String fastjsonStr = FastJsonUtil.toJSONString(bean);
        String jacksonStr = JacksonUtil.writeValueAsString(bean);

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
        String json = FastJsonUtil.toJSONString(bean);

        JsonBean gsonObj = GsonUtil.fromJson(json, JsonBean.class);
        JsonBean fastjsonObj = FastJsonUtil.parseObject(json, JsonBean.class);
        JsonBean jacksonObj = JacksonUtil.readValue(json, JsonBean.class);

        System.out.println();
        System.out.println("test Deserialize:");
        System.out.println("original:" + json);
        System.out.println("Fastjson:" + fastjsonObj);
        System.out.println("Gson:    " + gsonObj);
        System.out.println("Jackson: " + jacksonObj);

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
        JsonBean fastjsonObj = FastJsonUtil.parseObject(json, JsonBean.class);
        JsonBean gsonObj = GsonUtil.fromJson(json, JsonBean.class);
        JsonBean jacksonObj = JacksonUtil.readValue(json, JsonBean.class);

    }














}
