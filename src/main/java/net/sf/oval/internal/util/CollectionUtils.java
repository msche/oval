package net.sf.oval.internal.util;

import java.util.Collection;
import java.util.Map;

/**
 * Created by msche on 15-06-15.
 */
public final class CollectionUtils {

    public static CollectionType getType(Object value) {
        if (value == null) {
            return CollectionType.SINGLE;
        } else {
            if (value instanceof Collection<?>) {
                return CollectionType.COLLECTION;
            } else if (value instanceof Map<?,?>) {
                return CollectionType.MAP;
            } else if (value.getClass().isArray()) {
                return CollectionType.ARRAY;
            } else {
                return CollectionType.SINGLE;
            }
        }
    }

    public static CollectionType getType(Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("type may not be null");
        } else {
            if (Collection.class.isAssignableFrom(type)) {
                return CollectionType.COLLECTION;
            } else if (Map.class.isAssignableFrom(type)) {
                return CollectionType.MAP;
            } else if (type.isArray()) {
                return CollectionType.ARRAY;
            } else {
                return CollectionType.SINGLE;
            }
        }
    }

}
