package com.github.seregamorph.jackson.partial;

import java.lang.reflect.Field;
import java.util.Collection;

class TypeUtils {

    public static Class<?> extractElementClass(Field field) {
        // Support for other cases will be added when necessary
        Class<?> clazz = Collection.class.isAssignableFrom(field.getType())
                ? ResolvableType.forField(field).asCollection().resolveGeneric()
                : ResolvableType.forField(field).resolve(field.getType());
        return extractElementClass(clazz, field.getType());
    }

    private static Class<?> extractElementClass(Class<?> elementClass, Class<?> returnType) {
        if (elementClass == null) {
            return Object.class;
        }
        if (returnType.isArray()) {
            return elementClass.getComponentType();
        }
        return elementClass;
    }

    private TypeUtils() {
    }
}
