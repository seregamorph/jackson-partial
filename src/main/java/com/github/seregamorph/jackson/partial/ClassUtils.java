package com.github.seregamorph.jackson.partial;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

class ClassUtils {

    /**
     * Copied from apache commons
     *
     * @see org.apache.commons.lang3.ClassUtils#getAllInterfaces(Class)
     */
    static List<Class<?>> getAllInterfaces(Class<?> cls) {
        if (cls == null) {
            return null;
        }

        Set<Class<?>> interfacesFound = new LinkedHashSet<Class<?>>();
        getAllInterfaces(cls, interfacesFound);

        return new ArrayList<>(interfacesFound);
    }

    private static void getAllInterfaces(Class<?> cls, Set<Class<?>> interfacesFound) {
        while (cls != null) {
            Class<?>[] interfaces = cls.getInterfaces();

            for (Class<?> clazz : interfaces) {
                if (interfacesFound.add(clazz)) {
                    getAllInterfaces(clazz, interfacesFound);
                }
            }

            cls = cls.getSuperclass();
        }
    }

    private ClassUtils() {
    }
}
