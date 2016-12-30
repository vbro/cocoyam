package com.github.vbro.cocoyam;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created by vbro on 17 September 2016.
 */
public class IndexedSetTest {

    @Test
    public void testConstructors_IsIndexed_IndexComparison() {

        // using constructor with methods as String[]
        IndexedSet<String> is1 = new IndexedSet<>(String.class, new String[] {"trim", "length", "toString", "isEmpty", "hashCode"});
        is1.add("Hello World  ");
        is1.add("Hello World");
        is1.add("abc123");
        is1.add("Constructor");
        is1.add("");

        try {
            List<Method> methodList = new ArrayList<>();
            methodList.add(String.class.getDeclaredMethod("trim"));
            methodList.add(String.class.getDeclaredMethod("length"));
            methodList.add(String.class.getDeclaredMethod("toString"));
            methodList.add(String.class.getDeclaredMethod("isEmpty"));
            methodList.add(String.class.getDeclaredMethod("hashCode"));

            // using constructor with methods as Collection<Method>
            IndexedSet<String> is2 = new IndexedSet<>(String.class, methodList);
            is2.add("Hello World  ");
            is2.add("Hello World");
            is2.add("abc123");
            is2.add("Constructor");
            is2.add("");
            Assert.assertEquals("Json strings for both indexed sets should be the same", is1.toStringJson(), is2.toStringJson());
            Assert.assertTrue("Indexed sets should be equal", is1.equals(is2));
            Assert.assertTrue("Indexed sets should be equalsComparingIndexes", is1.equalsComparingIndexes(is2));

            // adding an existing element should have no effects
            is1.add("abc123");
            Assert.assertEquals("Json strings for both indexed sets should be the same", is1.toStringJson(), is2.toStringJson());
            Assert.assertTrue("Indexed sets should be equal", is1.equals(is2));
            Assert.assertTrue("Indexed sets should be equalsComparingIndexes", is1.equalsComparingIndexes(is2));

            IndexedSet<String> is3 = new IndexedSet<>(String.class, new String[] {"trim", "length"});
            is3.add("Hello World  ");
            is3.add("Hello World");
            is3.add("abc123");
            is3.add("Constructor");

            Assert.assertFalse("Indexed sets should not be equal", is2.equals(is3));
            Assert.assertFalse("Indexes should be different", is2.equalsComparingIndexes(is3));

            is3.add("");
            Assert.assertTrue("Indexed sets should be equal", is2.equals(is3));
            Assert.assertFalse("Indexes sets should be different", is2.equalsComparingIndexes(is3));


            //isIndexed
            Assert.assertTrue(is1.isIndexed("trim"));
            Assert.assertTrue(is1.isIndexed(String.class.getDeclaredMethod("trim")));
            Assert.assertTrue(is2.isIndexed("length"));
            Assert.assertTrue(is2.isIndexed(String.class.getDeclaredMethod("length")));

            Assert.assertFalse(is1.isIndexed("toUpperCase"));
            Assert.assertFalse(is1.isIndexed(String.class.getDeclaredMethod("toUpperCase")));
            Assert.assertFalse(is2.isIndexed("toUpperCase"));
            Assert.assertFalse(is2.isIndexed(String.class.getDeclaredMethod("toUpperCase")));

            Assert.assertFalse(is1.isIndexed("imaginaryMethod123"));
            Assert.assertFalse(is2.isIndexed("imaginaryMethod123"));
        }
        catch (NoSuchMethodException nsme) {
            Assert.fail("unable to create indexed set with List<Method>");
        }

        // test contructor with no Method list argument
        IndexedSet<String> is4 = new IndexedSet<>(String.class);
        // {"elements":[],"index":{"getClass":{},"toCharArray":{},"intern":{},"toLowerCase":{},"length":{},"isEmpty":{},"getBytes":{},"trim":{},"hashCode":{},"toUpperCase":{},"toString":{},"chars":{},"codePoints":{}}}
        String is4Json = "{\"elements\":[],\"index\":{\"getClass\":{},\"toCharArray\":{},\"intern\":{},\"toLowerCase\":{},\"length\":{},\"isEmpty\":{},\"getBytes\":{},\"trim\":{},\"hashCode\":{},\"toUpperCase\":{},\"toString\":{},\"chars\":{},\"codePoints\":{}}}";
        Assert.assertEquals(is4Json, is4.toStringJson());
    }

    @Test
    public void testClone() {
        IndexedSet<String> is1 = new IndexedSet<>(String.class);
        is1.add("Hello World  ");
        is1.add("Hello World");
        is1.add("abc123");
        is1.add("Constructor");

        IndexedSet<String> is2 = is1.clone();
        Assert.assertEquals("Cloned sets should be equal", is1, is2);
        Assert.assertTrue(is1.equalsComparingIndexes(is2));

        is2.add("123abc");
        Assert.assertNotEquals("Cloned sets should be different after adding element to one set", is1, is2);
        Assert.assertFalse(is1.equalsComparingIndexes(is2));

        is2 = is1.clone();
        is2.remove("Hello World");
        Assert.assertNotEquals("Cloned sets should be different after removing element from one set", is1, is2);
        Assert.assertFalse(is1.equalsComparingIndexes(is2));
    }

    @Test
    public void testClearIndex_ReIndex() {
        IndexedSet<String> is1 = new IndexedSet<>(String.class, new String[] {"trim", "length"});
        is1.add("Hello World");
        is1.add("abc123");

        // {"elements":["Hello World","abc123"],"index":{"trim":{"Hello World":["Hello World"],"abc123":["abc123"]},"length":{"6":["abc123"],"11":["Hello World"]}}}
        String preClear = "{\"elements\":[\"Hello World\",\"abc123\"],\"index\":{\"trim\":{\"Hello World\":[\"Hello World\"],\"abc123\":[\"abc123\"]},\"length\":{\"6\":[\"abc123\"],\"11\":[\"Hello World\"]}}}";

        // {"elements":["Hello World","abc123"],"index":{"trim":{},"length":{}}}
        String postClear = "{\"elements\":[\"Hello World\",\"abc123\"],\"index\":{\"trim\":{},\"length\":{}}}";

        Assert.assertEquals("Set index stated not as expected after adding elements", preClear, is1.toStringJson());

        is1.clearIndex();
        Assert.assertEquals("Set index stated not as expected after clearing index", postClear, is1.toStringJson());

        is1.reIndex();
        Assert.assertEquals("Set index stated not as expected after re-indexing", preClear, is1.toStringJson());
    }

    @Test
    public void testAdd_Get_Remove() {

        // setup IndexedSet
        IndexedSet<String> is1 = new IndexedSet<>(
                String.class, new String[] {"trim", "length", "toString", "isEmpty"});

        // add items
        is1.add("Hello World  ");
        is1.add("abc123");

        // test get with String methodName
        Assert.assertEquals(new HashSet<>(Arrays.asList(
                new String[] {"Hello World  "})),
                is1.get("trim", "Hello World"));

        Assert.assertEquals(new HashSet<>(Arrays.asList(
                new String[] {"Hello World  "})),
                is1.get("length", 13));

        Assert.assertEquals(new HashSet<>(Arrays.asList(
                new String[] {"abc123"})),
                is1.get("length", 6));

        Assert.assertEquals(new HashSet<>(Arrays.asList(
                new String[] {})),
                is1.get("toString", "does not exist 1"));

        Assert.assertEquals(new HashSet<>(Arrays.asList(
                new String[] {})),
                is1.get("toString", "does not exist 2"));

        Assert.assertEquals(new HashSet<>(Arrays.asList(
                new String[] {})),
                is1.get("isEmpty", true));

        Assert.assertEquals(new HashSet<>(Arrays.asList(
                new String[] {"Hello World  ", "abc123"})),
                is1.get("isEmpty", false));

        Assert.assertEquals(new HashSet<>(Arrays.asList(
                new String[] {})),
                is1.get("length", 100));

        // test get with method Method
        try {
            Assert.assertEquals(new HashSet<>(Arrays.asList(
                    new String[]{"Hello World  "})),
                    is1.get(String.class.getDeclaredMethod("trim"), "Hello World"));

            Assert.assertEquals(new HashSet<>(Arrays.asList(
                    new String[] {"Hello World  "})),
                    is1.get(String.class.getDeclaredMethod("length"), 13));

            Assert.assertEquals(new HashSet<>(Arrays.asList(
                    new String[] {"abc123"})),
                    is1.get(String.class.getDeclaredMethod("length"), 6));

            Assert.assertEquals(new HashSet<>(Arrays.asList(
                    new String[] {})),
                    is1.get(String.class.getDeclaredMethod("toString"), "does not exist 1"));

            Assert.assertEquals(new HashSet<>(Arrays.asList(
                    new String[] {})),
                    is1.get(String.class.getDeclaredMethod("toString"), "does not exist 2"));

            Assert.assertEquals(new HashSet<>(Arrays.asList(
                    new String[] {})),
                    is1.get(String.class.getDeclaredMethod("isEmpty"), true));

            Assert.assertEquals(new HashSet<>(Arrays.asList(
                    new String[] {"Hello World  ", "abc123"})),
                    is1.get(String.class.getDeclaredMethod("isEmpty"), false));

            Assert.assertEquals(new HashSet<>(Arrays.asList(
                    new String[] {})),
                    is1.get(String.class.getDeclaredMethod("length"), 100));
        }
        catch (NoSuchMethodException nsme) {
            Assert.fail("unable to getDeclaredMethod");
        }


        // remove an item and re-run some of the get statements from above
        is1.remove("abc123");

        Assert.assertEquals(new HashSet<>(Arrays.asList(
                new String[] {})),
                is1.get("length", 6));

        Assert.assertEquals(new HashSet<>(Arrays.asList(
                new String[] {"Hello World  "})),
                is1.get("isEmpty", false));

        Assert.assertEquals(new HashSet<>(Arrays.asList(
                new String[] {"Hello World  "})),
                is1.get("trim", "Hello World"));


        // add some more items, including using addAll
        is1.addAll(new ArrayList<>(Arrays.asList(
                new String[]{"Hello World", "Constructor", "does not exist 1"})));
        is1.add("");
        is1.add("accessibility");


        Assert.assertEquals(new HashSet<>(Arrays.asList(
                new String[] {"Hello World  ", "Hello World"})),
                is1.get("trim", "Hello World"));

        Assert.assertEquals(new HashSet<>(Arrays.asList(
                new String[] {""})),
                is1.get("isEmpty", true));

        Assert.assertEquals(new HashSet<>(Arrays.asList(
                new String[] {"Hello World  ", "accessibility"})),
                is1.get("length", 13));

        Assert.assertEquals(new HashSet<>(Arrays.asList(
                new String[] {"does not exist 1"})),
                is1.get("toString", "does not exist 1"));

        Assert.assertEquals(new HashSet<>(Arrays.asList(
                new String[] {})),
                is1.get("toString", "does not exist 2"));


        // remove items using removeAll
        is1.removeAll(new ArrayList<>(Arrays.asList(
                new String[]{"does not exist 1", "accessibility"})));

        Assert.assertEquals(new HashSet<>(Arrays.asList(
                new String[] {})),
                is1.get("toString", "does not exist 1"));

        Assert.assertEquals(new HashSet<>(Arrays.asList(
                new String[] {"Hello World  "})),
                is1.get("length", 13));


    }

    @Test
    public void testIndex() {
        // setup IndexedSet
        IndexedSet<String> is1 = new IndexedSet<>(
                String.class, new String[] {"length", "toUpperCase"});

        is1.add("aa");
        is1.add("bb");
        is1.add("cc");
        // {"elements":["aa","bb","cc"],"index":{"length":{"2":["aa","bb","cc"]},"toUpperCase":{"AA":["aa"],"BB":["bb"],"CC":["cc"]}}}
        String expectedJsonStep01 = "{\"elements\":[\"aa\",\"bb\",\"cc\"],\"index\":{\"length\":{\"2\":[\"aa\",\"bb\",\"cc\"]},\"toUpperCase\":{\"AA\":[\"aa\"],\"BB\":[\"bb\"],\"CC\":[\"cc\"]}}}";
        Assert.assertEquals(expectedJsonStep01, is1.toStringJson());

        is1.add("bb");
        Assert.assertEquals(expectedJsonStep01, is1.toStringJson());


        is1.add("xx", false);
        is1.add("yy", false);
        is1.add("zz", false);
        // {"elements":["aa","bb","cc","xx","yy","zz"],"index":{"length":{"2":["aa","bb","cc"]},"toUpperCase":{"AA":["aa"],"BB":["bb"],"CC":["cc"]}}}
        String expectedJsonStep03 = "{\"elements\":[\"aa\",\"bb\",\"cc\",\"xx\",\"yy\",\"zz\"],\"index\":{\"length\":{\"2\":[\"aa\",\"bb\",\"cc\"]},\"toUpperCase\":{\"AA\":[\"aa\"],\"BB\":[\"bb\"],\"CC\":[\"cc\"]}}}";
        Assert.assertEquals(expectedJsonStep03, is1.toStringJson());

        is1.remove("xx", false);
        // {"elements":["aa","bb","cc","yy","zz"],"index":{"length":{"2":["aa","bb","cc"]},"toUpperCase":{"AA":["aa"],"BB":["bb"],"CC":["cc"]}}}
        String expectedJsonStep04 = "{\"elements\":[\"aa\",\"bb\",\"cc\",\"yy\",\"zz\"],\"index\":{\"length\":{\"2\":[\"aa\",\"bb\",\"cc\"]},\"toUpperCase\":{\"AA\":[\"aa\"],\"BB\":[\"bb\"],\"CC\":[\"cc\"]}}}";
        Assert.assertEquals(expectedJsonStep04, is1.toStringJson());

        is1.remove("xx");
        Assert.assertEquals(expectedJsonStep04, is1.toStringJson());

        is1.remove("yy");
        // {"elements":["aa","bb","cc","zz"],"index":{"length":{"2":["aa","bb","cc"]},"toUpperCase":{"AA":["aa"],"BB":["bb"],"CC":["cc"]}}}
        String expectedJsonStep06 = "{\"elements\":[\"aa\",\"bb\",\"cc\",\"zz\"],\"index\":{\"length\":{\"2\":[\"aa\",\"bb\",\"cc\"]},\"toUpperCase\":{\"AA\":[\"aa\"],\"BB\":[\"bb\"],\"CC\":[\"cc\"]}}}";
        Assert.assertEquals(expectedJsonStep06, is1.toStringJson());

        is1.remove("aa", false);
        // {"elements":["bb","cc","zz"],"index":{"length":{"2":["aa","bb","cc"]},"toUpperCase":{"AA":["aa"],"BB":["bb"],"CC":["cc"]}}}
        String expectedJsonStep07 = "{\"elements\":[\"bb\",\"cc\",\"zz\"],\"index\":{\"length\":{\"2\":[\"aa\",\"bb\",\"cc\"]},\"toUpperCase\":{\"AA\":[\"aa\"],\"BB\":[\"bb\"],\"CC\":[\"cc\"]}}}";
        Assert.assertEquals(expectedJsonStep07, is1.toStringJson());

        is1.remove("aa");
        // {"elements":["bb","cc","zz"],"index":{"length":{"2":["bb","cc"]},"toUpperCase":{"AA":[],"BB":["bb"],"CC":["cc"]}}}
        String expectedJsonStep08 = "{\"elements\":[\"bb\",\"cc\",\"zz\"],\"index\":{\"length\":{\"2\":[\"bb\",\"cc\"]},\"toUpperCase\":{\"AA\":[],\"BB\":[\"bb\"],\"CC\":[\"cc\"]}}}";
        Assert.assertEquals(expectedJsonStep08, is1.toStringJson());

        is1.remove("bb");
        // {"elements":["cc","zz"],"index":{"length":{"2":["cc"]},"toUpperCase":{"AA":[],"BB":[],"CC":["cc"]}}}
        String expectedJsonStep09 = "{\"elements\":[\"cc\",\"zz\"],\"index\":{\"length\":{\"2\":[\"cc\"]},\"toUpperCase\":{\"AA\":[],\"BB\":[],\"CC\":[\"cc\"]}}}";
        Assert.assertEquals(expectedJsonStep09, is1.toStringJson());

        is1.remove("bb");
        Assert.assertEquals(expectedJsonStep09, is1.toStringJson());

        is1.reIndex();
        // {"elements":["cc","zz"],"index":{"length":{"2":["cc","zz"]},"toUpperCase":{"CC":["cc"],"ZZ":["zz"]}}}
        String expectedJsonStep11 = "{\"elements\":[\"cc\",\"zz\"],\"index\":{\"length\":{\"2\":[\"cc\",\"zz\"]},\"toUpperCase\":{\"CC\":[\"cc\"],\"ZZ\":[\"zz\"]}}}";
        Assert.assertEquals(expectedJsonStep11, is1.toStringJson());
    }

    @Test
    public void testExceptions() {
        boolean isExceptionThrown = false;

        // construct with non existent method name
        try {
            IndexedSet<String> is1 = new IndexedSet<>(String.class, new String[]{"trim", "length", "toString", "isEmpty", "hashCode", "imaginaryMethod"});
        }
        catch (IllegalArgumentException e) {
            isExceptionThrown = true;
        }
        Assert.assertTrue("IllegalArgumentException should be thrown", isExceptionThrown);
        isExceptionThrown = false;

        // construct with Method from different class
        try {
            List<Method> methodList = new ArrayList<>();
            methodList.add(List.class.getDeclaredMethod("hashCode"));
            methodList.add(String.class.getDeclaredMethod("length"));

            // using constructor with methods as Collection<Method>
            IndexedSet<String> is1 = new IndexedSet<>(String.class, methodList);
        }
        catch (NoSuchMethodException nsme) {
            Assert.fail("unable to create indexed set with List<Method>");
        }
        catch (IllegalArgumentException e) {
            isExceptionThrown = true;
        }
        Assert.assertTrue("IllegalArgumentException should be thrown", isExceptionThrown);
        isExceptionThrown = false;


        // add null to IndexedSet
        IndexedSet<String> is1 = new IndexedSet<>(String.class, new String[]{"trim", "length", "toString", "isEmpty"});
        try {
            is1.add(null);
        }
        catch (NullPointerException e) {
            isExceptionThrown = true;
        }
        Assert.assertTrue("NullPointerException should be thrown", isExceptionThrown);
        isExceptionThrown = false;

        is1.add("Hello");
        is1.add("World");

        // get using un-indexed methodName string
        try {
            is1.get("toUpperCase", "HELLO");
        }
        catch (IllegalArgumentException e) {
            isExceptionThrown = true;
        }
        Assert.assertTrue("IllegalArgumentException should be thrown", isExceptionThrown);
        isExceptionThrown = false;


        // get using non-existent method name
        try {
            is1.get("imaginaryMethod123", "World");
        }
        catch (IllegalArgumentException e) {
            isExceptionThrown = true;
        }
        Assert.assertTrue("IllegalArgumentException should be thrown", isExceptionThrown);
        isExceptionThrown = false;


        // get un-indexed Method
        try {
            is1.get(String.class.getDeclaredMethod("toUpperCase"), "HELLO");
        }
        catch (NoSuchMethodException nsme) {
            Assert.fail("unable to getDeclaredMethod");
        }
        catch (IllegalArgumentException e) {
            isExceptionThrown = true;
        }
        Assert.assertTrue("IllegalArgumentException should be thrown", isExceptionThrown);
        isExceptionThrown = false;


        // get using Method from a different class
        try {
            is1.get(List.class.getDeclaredMethod("isEmpty"), "HELLO");
        }
        catch (NoSuchMethodException nsme) {
            Assert.fail("unable to getDeclaredMethod");
        }
        catch (IllegalArgumentException e) {
            isExceptionThrown = true;
        }
        Assert.assertTrue("IllegalArgumentException should be thrown", isExceptionThrown);
        isExceptionThrown = false;
    }
}
