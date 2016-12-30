package com.github.vbro.cocoyam;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import javax.json.JsonValue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vbro on 20 Septermber 2016.
 */
public class JsonUtilTest {

    JsonValue jv = null;

    @Test
    public void testLiterals() {
        jv = JsonUtil.toJson(3);
        assertEquals("testLiterals: Values do not match", "3", jv.toString());

        jv = JsonUtil.toJson(-2.718);
        assertEquals("testLiterals: Values do not match", "-2.718", jv.toString());

        jv = JsonUtil.toJson(6022141410704090840L);
        assertEquals("testLiterals: Values do not match", "6022141410704090840", jv.toString());

        jv = JsonUtil.toJson(true);
        assertEquals("testLiterals: Values do not match", "true", jv.toString());

        jv = JsonUtil.toJson("HelloWorld");
        assertEquals("testLiterals: Values do not match", "\"HelloWorld\"", jv.toString());
    }

    @Test
    public void testObjectArrays() {
        String[] strings1 = {"hello world", "abc", "123", "k"};
        String[] strings2 = {"this", "is", "strings2"};
        String[][] twoDStrings = {strings1, strings2};

        jv = JsonUtil.toJson(new String[]{});
        assertEquals("testObjectArrays: Values do not match", "[]", jv.toString());

        jv = JsonUtil.toJson(strings1);
        assertEquals("testObjectArrays: Values do not match",
                "[\"hello world\",\"abc\",\"123\",\"k\"]", jv.toString());

        List<String> strings1List = new ArrayList();
        strings1List.add("hello world");
        strings1List.add("abc");
        strings1List.add("123");
        strings1List.add("k");
        jv = JsonUtil.toJson(strings1List);
        assertEquals("testObjectArrays: Values do not match",
                "[\"hello world\",\"abc\",\"123\",\"k\"]", jv.toString());

        jv = JsonUtil.toJson(twoDStrings);
        assertEquals("testObjectArrays: Values do not match",
                "[[\"hello world\",\"abc\",\"123\",\"k\"],[\"this\",\"is\",\"strings2\"]]", jv.toString());
    }

    @Test
    public void testPrimitiveArrays() {
        jv = JsonUtil.toJson(new float[] {});
        assertEquals("testPrimitiveArrays: Values do not match", "[]", jv.toString());

        jv = JsonUtil.toJson(new ArrayList<Integer>());
        assertEquals("testPrimitiveArrays: Values do not match", "[]", jv.toString());

        int[] intArray = {4, 5, 6, 7};
        jv = JsonUtil.toJson(intArray);
        assertEquals("testPrimitiveArrays: Values do not match", "[4,5,6,7]", jv.toString());

        Integer[] integerArray = {4, 5, 6, 7};
        jv = JsonUtil.toJson(integerArray);
        assertEquals("testPrimitiveArrays: Values do not match", "[4,5,6,7]", jv.toString());

        List<Integer> integerList = new ArrayList();
        integerList.add(4);integerList.add(5);integerList.add(6);integerList.add(7);
        jv = JsonUtil.toJson(integerList);
        assertEquals("testPrimitiveArrays: Values do not match", "[4,5,6,7]", jv.toString());

        int[][] int2DArray = {new int[]{1,2,3}, new int[]{4,5,6,7}};
        jv = JsonUtil.toJson(int2DArray);
        assertEquals("testPrimitiveArrays: Values do not match", "[[1,2,3],[4,5,6,7]]", jv.toString());

        List<Long> longList = new ArrayList();
        longList.add(10000000000L);
        longList.add(2L);
        longList.add(667408L);
        longList.add(6022141410704090840L);

        jv = JsonUtil.toJson(longList);
        assertEquals("testPrimitiveArrays: Values do not match",
                "[10000000000,2,667408,6022141410704090840]", jv.toString());

        boolean[] b = {true, true, false};
        jv = JsonUtil.toJson(b);
        assertEquals("testPrimitiveArrays: Values do not match", "[true,true,false]", jv.toString());

        Boolean[] booleanArray = {true, true, false};
        jv = JsonUtil.toJson(booleanArray);
        assertEquals("testPrimitiveArrays: Values do not match", "[true,true,false]", jv.toString());

        Character[] characterArray = {'a', '2', 'C'};
        jv = JsonUtil.toJson(characterArray);
        assertEquals("testPrimitiveArrays: Values do not match", "[\"a\",\"2\",\"C\"]", jv.toString());

        char[] charArray = {'a', '2', 'C'};
        jv = JsonUtil.toJson(charArray);
        assertEquals("testPrimitiveArrays: Values do not match", "[\"a\",\"2\",\"C\"]", jv.toString());
    }

    @Test
    public void testObjects() {

        Map<Integer, Boolean> m = new HashMap();
        m.put(1, true);
        m.put(2, false);
        m.put(3, null);
        m.put(null, false);
        jv = JsonUtil.toJson(m);
        assertEquals("testObjects", "{\"null\":false,\"1\":true,\"2\":false,\"3\":null}", jv.toString());


        Map<Character, List<BigDecimal>> mapList = new HashMap();

        List<BigDecimal> bigDecimalListList1 = new ArrayList();
        bigDecimalListList1.add(new BigDecimal(1.2).setScale(1, BigDecimal.ROUND_HALF_UP));
        bigDecimalListList1.add(new BigDecimal(3.45).setScale(2, BigDecimal.ROUND_HALF_UP));

        List<BigDecimal> bigDecimalListList2 = new ArrayList();
        bigDecimalListList2.add(new BigDecimal(3.14159).setScale(5, BigDecimal.ROUND_HALF_UP));
        bigDecimalListList2.add(new BigDecimal(6.022).setScale(3, BigDecimal.ROUND_HALF_UP));
        bigDecimalListList2.add(new BigDecimal(9.81).setScale(2, BigDecimal.ROUND_HALF_UP));
        bigDecimalListList2.add(new BigDecimal(2.7182818285).setScale(10, BigDecimal.ROUND_HALF_UP));

        mapList.put('a', bigDecimalListList1);
        mapList.put(null, new ArrayList());
        mapList.put('b', bigDecimalListList2);
        mapList.put('c', null);
        mapList.put('d', new ArrayList());
        jv = JsonUtil.toJson(mapList);
        assertEquals("testObjects",
                "{\"null\":[],\"a\":[1.2,3.45],\"b\":[3.14159,6.022,9.81,2.7182818285],\"c\":null,\"d\":[]}",
                jv.toString());

        Map<Object, Object> objectMap = new HashMap();
        objectMap.put(null, null);
        objectMap.put(1, null);
        objectMap.put(2, new ArrayList());
        objectMap.put(3, new HashMap());
        objectMap.put("four", 4);
        objectMap.put("five", "five");
        objectMap.put(6, "six");
        objectMap.put(true, true);
        objectMap.put("map", m);
        objectMap.put("mapList", mapList);
        objectMap.put("list", bigDecimalListList1);
        jv = JsonUtil.toJson(objectMap);
        assertEquals("testObjects",
                "{\"null\":null,\"1\":null,\"mapList\":{\"null\":[],\"a\":[1.2,3.45],\"b\":" +
                        "[3.14159,6.022,9.81,2.7182818285],\"c\":null,\"d\":[]},\"2\":[],\"3\":{},\"four\":4,\"6\":" +
                        "\"six\",\"list\":[1.2,3.45],\"five\":\"five\",\"map\":{\"null\":false,\"1\":true,\"2\":" +
                        "false,\"3\":null},\"true\":true}",
                jv.toString());
    }
}
