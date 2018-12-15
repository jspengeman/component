package com.jspengeman.tools.component.util

import com.google.common.base.VerifyException
import com.google.common.collect.ImmutableList;
import spock.lang.Specification

public class ObjectCreatorSpec extends Specification {
    def "null classType throws VerifyException"() {
        when: ObjectCreator.create(null, ImmutableList.of())
        then: thrown(VerifyException)
    }

    def "null arguments throws VerifyException"() {
        when: ObjectCreator.create(Object.class, null)
        then: thrown(VerifyException)
    }

    def "no matching constructor found throws IllegalArgumentException"() {
        when: ObjectCreator.create(TestClass.class, ImmutableList.of(1));
        then: thrown(IllegalArgumentException)
    }

    def "cannot access found constructor throws IllegalArgumentException"() {
        when: ObjectCreator.create(TestClass.class, ImmutableList.of("1", "2"));
        then: thrown(IllegalArgumentException)
    }

    def "cannot instantiate class throws IllegalArgumentException"() {
        when: ObjectCreator.create(List.class, ImmutableList.of());
        then: thrown(IllegalArgumentException)
    }

    // TODO: This does not work because the way the types "change" at runtime.
    def "successfully constructs object with arguments"() {
        when:
            def result = ObjectCreator.create(
                    TestClass.class,
                    ImmutableList.of(
                        ImmutableList.of(1, 2, 3),
                        "string",
                        2.0))
        then: result != null;
    }

    def "successfully constructs object with no arguments"() {
        when:
            def result = ObjectCreator.create(
                TestClass.class,
                ImmutableList.of())
        then: result != null;
    }

    private static class TestClass {

        public TestClass() {}

        private TestClass(String str1, String str2) {}

        public TestClass(ImmutableList<Integer> nums,
                         String str,
                         double dbl) {}
    }
}
