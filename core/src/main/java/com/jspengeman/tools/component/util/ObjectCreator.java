package com.jspengeman.tools.component.util;


import com.google.common.collect.ImmutableList;

import java.lang.reflect.InvocationTargetException;

public final class ObjectCreator {
    private ObjectCreator() {}

    public static Object newInstance(
            Class classType,
            ImmutableList<Object> arguments) {
        // get the types for the constructor.
        Class[] paramTypes = new Class[arguments.size()];
        for (int i = 0; i < paramTypes.length; i++) {
            paramTypes[i] = arguments.get(i).getClass();
        }

        // get the params for the constructor.
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
            throw new IllegalArgumentException("Unable to construct component.");
        }
    }
}
