package com.jspengeman.tools.component.util;


import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;

import java.lang.reflect.InvocationTargetException;

public final class ObjectCreator {
    private ObjectCreator() {}

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
            return classType
                    .asSubclass(classType)
                    .getConstructor(paramTypes)
                    .newInstance(params);
        } catch (NoSuchMethodException |
                InstantiationException |
                IllegalAccessException |
                InvocationTargetException e) {
            throw new IllegalArgumentException("Unable to create.");
        }
    }
}
