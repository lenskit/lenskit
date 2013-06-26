package org.grouplens.lenskit.collections;

/**
 * Helper class for implementing int pointers.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class AbstractIntPointer implements IntPointer {
    /**
     * {@inheritDoc}
     * This implementation delegates to {@link #getInt()}.
     */
    @Override
    public Integer get() {
        return getInt();
    }
}
