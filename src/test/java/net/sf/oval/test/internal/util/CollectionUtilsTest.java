package net.sf.oval.test.internal.util;

import junit.framework.TestCase;
import net.sf.oval.internal.util.CollectionType;
import net.sf.oval.internal.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by msche on 20-06-15.
 */
public class CollectionUtilsTest  extends TestCase {

    /**
     * Verifies correct type enums are returned when particular class is passed
     */
    public void testGetTypeClass() {
        assertEquals(CollectionType.COLLECTION, CollectionUtils.getType(Collection.class));
        assertEquals(CollectionType.COLLECTION, CollectionUtils.getType(List.class));
        assertEquals(CollectionType.COLLECTION, CollectionUtils.getType(Set.class));

        assertEquals(CollectionType.MAP, CollectionUtils.getType(Map.class));

        assertEquals(CollectionType.ARRAY, CollectionUtils.getType(new int[10].getClass()));
    }
}
