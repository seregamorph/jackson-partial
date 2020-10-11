package com.github.seregamorph.jackson.partial;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Base class to support partial Jackson nodes serialization and deserialization.
 */
// For lombok to work properly, use hard coded values instead of static final ones
//@EqualsAndHashCode(doNotUseGetters = true, exclude = {"payloadClass", "partialProperties"})
//@FieldNameConstants(level = PACKAGE)
public abstract class PartialPayload {

    @JsonIgnore
    private Class<?> payloadClass;

    @JsonIgnore
    private Map<String, Object> partialProperties;

    void setPayloadClass(Class<?> payloadClass) {
        this.payloadClass = payloadClass;
    }

    Class<?> getPayloadClass() {
        return payloadClass == null ? this.getClass() : payloadClass;
    }

    void setPartialProperties(Map<String, Object> properties) {
        partialProperties = properties == null ? null : new HashMap<>(properties);
    }

    void setPartialProperty(String property, Object value) {
        if (partialProperties == null) {
            partialProperties = new HashMap<>();
        }
        partialProperties.put(property, value);
    }

    Map<String, Object> getPartialProperties() {
        return partialProperties == null ? Collections.emptyMap() : Collections.unmodifiableMap(partialProperties);
    }

    boolean isPartialPropertiesInitialized() {
        return partialProperties != null;
    }

    public boolean hasPartialProperty(String property) {
        return partialProperties != null && partialProperties.containsKey(property);
    }

    /**
     * Extract the actual payload - an instance of the same type with only properties that exist in
     * {@link #partialProperties}.
     *
     * @return the actual payload.
     */
    @SuppressWarnings("unchecked")
    public <T> T extractPayload() {
        Object p;
        try {
            p = getPayloadClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to instantiate class", e);
        }
        copy(this, p);
        return (T) p;
    }

    public void copyTo(Object target) {
        copy(this, target);
    }

    /**
     * Copy partial properties (both actual data and metadata) from the source to the target.
     *
     * @param source the source
     * @param target the target
     */
    @SuppressWarnings("unchecked")
    private static void copy(PartialPayload source, Object target) {
        requireNonNull(source, "source");
        requireNonNull(target, "target");

        Map<String, Object> properties = source.getPartialProperties();

        if (target instanceof PartialPayload) {
            ((PartialPayload) target).setPartialProperties(properties);
        }

        Set<Map.Entry<String, Object>> entries = properties.entrySet();

        for (Map.Entry<String, Object> entry : entries) {
            Field sourceField = FieldUtils.getField(source.getPayloadClass(), entry.getKey(), true);

            if (sourceField == null) {
                continue;
            }

            PayloadId payloadId = sourceField.getAnnotation(PayloadId.class);

            if (payloadId == null) {
                payloadId = TypeUtils.extractElementClass(sourceField).getAnnotation(PayloadId.class);
            }

            String targetFieldName = entry.getKey();

            if (!(target instanceof PartialPayload)) {
                Renamed renamed = sourceField.getAnnotation(Renamed.class);

                if (renamed != null) {
                    targetFieldName = renamed.value();
                }
            }

            Object sourceValue = entry.getValue();
            Object targetValue = getFieldValue(target, targetFieldName);

            if (sourceValue instanceof PartialPayload) {
                // Only copy fields if the IDs are the same
                // If target value is null, or target value is not null but has a different ID, init new object instead
                if (targetValue != null && shouldCopy(payloadId, sourceValue, targetValue)) {
                    copy((PartialPayload) sourceValue, targetValue);
                } else {
                    Field targetField = FieldUtils.getField(target.getClass(), targetFieldName, true);
                    targetValue = BeanUtils.instantiateClass(targetField.getType());
                    copy((PartialPayload) sourceValue, targetValue);
                    setFieldValue(target, targetFieldName, targetValue);
                }
            } else if (sourceValue instanceof Collection && targetValue instanceof Collection) {
                copyCollection(payloadId, (Collection<Object>) sourceValue, (Collection<Object>) targetValue);
            } else {
                setFieldValue(target, targetFieldName, sourceValue);
            }
        }
    }

    private static void copyCollection(PayloadId payloadId, Collection<Object> source, Collection<Object> target) {
        // Replace the elements, but not the collection itself. This ensures that, if the target collection is a
        // JPA-managed collection, then after all elements have been replaced, it's still a managed collection.
        Collection<Object> backupCollection = new ArrayList<>(target);
        target.clear();

        for (Object sourceElement : source) {
            if (sourceElement instanceof PartialPayload) {
                Object targetElement = backupCollection.stream()
                        .filter(element -> shouldCopy(payloadId, sourceElement, element))
                        .findFirst()
                        .orElse(null);
                if (targetElement != null) {
                    target.add(targetElement);
                    copy((PartialPayload) sourceElement, targetElement);
                    continue;
                }
            }

            target.add(sourceElement);
        }
    }

    private static boolean shouldCopy(PayloadId payloadId, Object first, Object second) {
        if (payloadId == null || payloadId.value().length == 0) {
            return first instanceof PartialPayload;
        }

        for (String field : payloadId.value()) {
            Object firstValue = ClassUtils.getFieldValue(first, field);
            Object secondValue = ClassUtils.getFieldValue(second, field);

            if (!Objects.equals(firstValue, secondValue)) {
                return false;
            }
        }

        return true;
    }
}
