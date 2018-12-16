package com.jspengeman.tools.component.util;


import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;

import java.lang.reflect.Constructor;
import java.util.Optional;

import org.apache.commons.lang3.reflect.ConstructorUtils;

/**
 * Responsible for creating objects using reflection.
 */
public final class ObjectCreator {

    private ObjectCreator() {}

    /**
     * Create an object given the class and the parameters used to infer
     * what constructor to use to create that class.
     *
     * @param classType
     *      The class to create.
     * @param arguments
     *      The arguments to infer the constructor for. All arguments are
     *      passed to the constructor in the order they were present in the
     *      list.
     * @return
     *      The created object.
     * @throws
     *      IllegalArgumentException if class creation fails because of
     *      a problem with reflection. The class/constructor could be
     *      inaccessible or a constructor with the given types does not
     *      exist.
     */
    public static Object create(Class classType,
                                ImmutableList<Object> arguments) {
        Verify.verifyNotNull(classType, "classType cannot be null.");
        Verify.verifyNotNull(arguments, "arguments cannot be null.");

        Class[] paramTypes = new Class[arguments.size()];
        for (int i = 0; i < paramTypes.length; i++) {
            paramTypes[i] = arguments.get(i).getClass();
        }

        Object[] params = new Object[arguments.size()];
        for (int i = 1; i < params.length; i++) {
            params[i] = arguments.get(i);
        }

        try {
            Optional<Constructor> maybeCtor =
                getMatchingAccessibleConstructor(classType, paramTypes);
            if (maybeCtor.isPresent()) {
                return maybeCtor.get().newInstance(params);
            } else {
                throw new ReflectiveOperationException("Constructor not found.");
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Unable to create class.");
        }
    }

    @SuppressWarnings("unchecked")
    private static Optional<Constructor> getMatchingAccessibleConstructor(
            Class classType ,
            Class[] paramTypes) {
        Constructor ctor = ConstructorUtils
            .getMatchingAccessibleConstructor(classType, paramTypes);
        return Optional.ofNullable(ctor);
    }
}
