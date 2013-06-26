package org.grouplens.lenskit.collections;

/**
 * A pointer over integers.
 *
 * @since 1.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface IntPointer extends Pointer<Integer> {
    /**
     * Get the integer at the pointer's current location.
     * @return The pointer's current integer.
     * @throws java.util.NoSuchElementException if the pointer is out-of-bounds.
     */
    int getInt();
}
