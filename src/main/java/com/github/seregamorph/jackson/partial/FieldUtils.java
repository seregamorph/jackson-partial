package com.github.seregamorph.jackson.partial;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

class FieldUtils {

    /**
     * Copied from apache commons
     *
     * @see org.apache.commons.lang3.reflect.FieldUtils#getField(Class, String, boolean)
     */
    public static Field getField(Class<?> cls, String fieldName, boolean forceAccess) {
        Validate.isTrue(cls != null, "The class must not be null");
        Validate.isTrue(StringUtils.isNotBlank(fieldName), "The field name must not be blank/empty");
        // FIXME is this workaround still needed? lang requires Java 6
        // Sun Java 1.3 has a bugged implementation of getField hence we write the
        // code ourselves

        // getField() will return the Field object with the declaring class
        // set correctly to the class that declares the field. Thus requesting the
        // field on a subclass will return the field from the superclass.
        //
        // priority order for lookup:
        // searchclass private/protected/package/public
        // superclass protected/package/public
        // private/different package blocks access to further superclasses
        // implementedinterface public

        // check up the superclass hierarchy
        for (Class<?> acls = cls; acls != null; acls = acls.getSuperclass()) {
            try {
                final Field field = acls.getDeclaredField(fieldName);
                // getDeclaredField checks for non-public scopes as well
                // and it returns accurate results
                if (!Modifier.isPublic(field.getModifiers())) {
                    if (forceAccess) {
                        field.setAccessible(true);
                    } else {
                        continue;
                    }
                }
                return field;
            } catch (final NoSuchFieldException ex) { // NOPMD
                // ignore
            }
        }
        // check the public interface case. This must be manually searched for
        // incase there is a public supersuperclass field hidden by a private/package
        // superclass field.
        Field match = null;
        for (final Class<?> class1 : ClassUtils.getAllInterfaces(cls)) {
            try {
                final Field test = class1.getField(fieldName);
                Validate.isTrue(match == null, "Reference to field %s is ambiguous relative to %s"
                        + "; a matching field exists on two or more implemented interfaces.", fieldName, cls);
                match = test;
            } catch (final NoSuchFieldException ex) { // NOPMD
                // ignore
            }
        }
        return match;
    }

    private FieldUtils() {
    }
}
