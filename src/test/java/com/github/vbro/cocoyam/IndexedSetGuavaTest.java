package com.github.vbro.cocoyam;

import com.google.common.collect.testing.SetTestSuiteBuilder;
import java.util.Set;

import com.google.common.collect.testing.TestStringSetGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.SetFeature;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Created by vbro on 03 October 2016.
 */
public class IndexedSetGuavaTest extends TestCase {

    public static Test suite() {
        return new IndexedSetGuavaTest().allTests();
    }

    public Test allTests() {
        TestSuite suite =
                new TestSuite("com.github.vbro.cocoyam.IndexedSet");
        suite.addTest(serializableTestSuite());
        suite.addTest(testSuite());
        return suite;
    }

    public Test serializableTestSuite() {
        return SetTestSuiteBuilder.using(
                new TestStringSetGenerator(){
                    @Override
                    protected Set<String> create(String[] elements) {
                        String[] methods = {"trim", "toString", "length"};
                        IndexedSerializableSet<String> set = new IndexedSerializableSet(String.class, methods);
                        for (String s: elements) {
                            set.add(s);
                        }
                        return set;
                    }
                }).named("IndexedSerializableSetGuavaTest")
                .withFeatures(
                        CollectionFeature.SERIALIZABLE,
                        CollectionSize.ANY,
                        SetFeature.GENERAL_PURPOSE
                )
                .createTestSuite();
    }

    public Test testSuite() {
        return SetTestSuiteBuilder.using(
                new TestStringSetGenerator(){
                    @Override
                    protected Set<String> create(String[] elements) {
                        String[] methods = {"trim", "toString", "length"};
                        IndexedSet<String> set = new IndexedSet(String.class, methods);
                        for (String s: elements) {
                            set.add(s);
                        }
                        return set;
                    }
                }).named("IndexedSetGuavaTest")
                .withFeatures(
                        CollectionSize.ANY,
                        SetFeature.GENERAL_PURPOSE
                )
                .createTestSuite();
    }
}
