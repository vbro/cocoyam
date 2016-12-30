package com.github.vbro.cocoyam;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by vbro on 20 September 2016.
 */
public class JsonUtil {

    private static String PLACE_HOLDER_KEY = "place_holder_key";

    /**
     * @param o
     * @return A compact JSON string representation of Object o.
     */
    public static String toJsonString(Object o) {
        return toJson(o).toString();
    }

    /**
     * @param o
     * @return A pretty-printed JSON string representation of Object o.
     */
    public static String toJsonStringPrettyPrint(Object o) {

        JsonStructure json = null;
        if (o instanceof JsonStructure) {
            json = (JsonStructure)o;
        }
        else if (o instanceof JsonValue) {
            return o.toString();
        }
        else {
            return toJsonStringPrettyPrint(toJson(o));
        }

        StringWriter stringWriter = new StringWriter();
        Map<String, Boolean> config = new HashMap();
        config.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonWriterFactory writerFactory = Json.createWriterFactory(config);
        JsonWriter jsonWriter = writerFactory.createWriter(stringWriter);

        jsonWriter.write(json);
        jsonWriter.close();

        return stringWriter.toString();
    }

    /**
     * @param o
     * @return a JsonValue representation of java Object o.
     * A java.util.Map is represented as a JSON object with key "null" or key.toString.
     * An array or java.util.Iterable is represented as a JSON array.
     * Primitives are represented by their corresponding JSON primitive.
     * All other objects are represented as strings with value o.toString().
     * Values in maps, arrays and iterables are recursively evaluated by this method.
     */
    public static JsonValue toJson(Object o) {
        if (o == null) {
            return JsonValue.NULL;
        }
        else if (o instanceof JsonValue) {
            return (JsonValue)o;
        }
        else if (o instanceof Map) {
            JsonObjectBuilder job = Json.createObjectBuilder();
            Map m = (Map)o;
            for (Object key : m.keySet()) {
                job.add(getJsonKey(key), toJson(m.get(key)));
            }
            return job.build();
        }
        else if (o instanceof Iterable || o.getClass().isArray()) {
            Iterable i;
            JsonArrayBuilder jab = Json.createArrayBuilder();
            if (o instanceof  Iterable) {
                i = (Iterable)o;
            }
            else if (o instanceof Object[]) {
                Object[] arrayObject = (Object[])o;
                i = Arrays.asList(arrayObject);
            }
            else {
                i = getPrimitiveArray(o);
            }
            for (Object item : i) {
                jab.add(toJson(item));
            }
            return jab.build();
        }
        else {
            return getJsonValue(o);
        }
    }

    /**
     * @param o
     * @return java.lang.Iterable of boxed primitive values contained in array o.
     */
    private static Iterable getPrimitiveArray(Object o) {

        if (o instanceof int[]) {
            return Arrays.stream((int[])o).boxed().collect(
                    Collectors.toList());
        }
        else if (o instanceof double[]) {
            return Arrays.stream((double[])o).boxed().collect(
                    Collectors.toList());
        }
        else if (o instanceof long[]) {
            return Arrays.stream((long[])o).boxed().collect(
                    Collectors.toList());
        }
        else if (o instanceof boolean[]) {
            List<Boolean> objList = new ArrayList();
            for (boolean primitiveValue : (boolean[])o) {
                objList.add(primitiveValue);
            }
            return objList;
        }
        else if (o instanceof char[]) {
            List<Character> objList = new ArrayList();
            for (char primitiveValue : (char[])o) {
                objList.add(primitiveValue);
            }
            return objList;
        }
        else if (o instanceof byte[]) {
            List<Byte> objList = new ArrayList();
            for (byte primitiveValue : (byte[])o) {
                objList.add(primitiveValue);
            }
            return objList;
        }
        else if (o instanceof short[]) {
            List<Short> objList = new ArrayList();
            for (short primitiveValue : (short[])o) {
                objList.add(primitiveValue);
            }
            return objList;
        }
        else if (o instanceof float[]) {
            List<Float> objList = new ArrayList();
            for (float primitiveValue : (float[])o) {
                objList.add(primitiveValue);
            }
            return objList;
        }
        throw new IllegalArgumentException(String.format(
                "Cannot convert object (%s) of type %s to java.lang.Iterable",
                o.toString(), o.getClass().getName()));
    }

    /**
     * @param o
     * @return a JsonValue representation of primitive object o,
     * i.e. Object o is neither a JSON object nor a JSON array.
     */
    private static JsonValue getJsonValue(Object o) {
        JsonObjectBuilder job = Json.createObjectBuilder();
        if (o instanceof Boolean) {
            if ((Boolean)o) {
                return JsonValue.TRUE;
            }
            else {
                return JsonValue.FALSE;
            }
        }
        else if (o instanceof BigDecimal) {
            job.add(PLACE_HOLDER_KEY, (BigDecimal)o);
            return job.build().getJsonNumber(PLACE_HOLDER_KEY);
        }
        else if (o instanceof BigInteger) {
            job.add(PLACE_HOLDER_KEY, (BigInteger)o);
            return job.build().getJsonNumber(PLACE_HOLDER_KEY);
        }
        else if (o instanceof Double) {
            job.add(PLACE_HOLDER_KEY, (Double)o);
            return job.build().getJsonNumber(PLACE_HOLDER_KEY);
        }
        else if (o instanceof Integer) {
            job.add(PLACE_HOLDER_KEY, (Integer)o);
            return job.build().getJsonNumber(PLACE_HOLDER_KEY);
        }
        else if (o instanceof Long) {
            job.add(PLACE_HOLDER_KEY, (Long)o);
            return job.build().getJsonNumber(PLACE_HOLDER_KEY);
        }
        else {
            job.add(PLACE_HOLDER_KEY, o.toString());
            return job.build().getJsonString(PLACE_HOLDER_KEY);
        }
    }

    /**
     * @param o
     * @return a string representation of Object o that can be used as a json object key.
     */
    private static String getJsonKey(Object o) {
        return o==null ? "null" : o.toString();
    }
}
