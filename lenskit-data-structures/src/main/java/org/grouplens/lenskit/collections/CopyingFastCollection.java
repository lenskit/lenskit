package org.grouplens.lenskit.collections;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

import javax.annotation.Nullable;
import java.util.AbstractCollection;
import java.util.Iterator;

/**
 * Abstract fast collection that implements {@link #iterator()} in terms of
 * {@link #fastIterator()} and {@link #copy(E)}.
 *
 * @author Michael Ekstrand
 * @since 1.1
 */
public abstract class CopyingFastCollection<E> extends AbstractCollection<E> implements FastCollection<E> {
    private final Function<E, E> copyFunction = new Function<E, E>() {
        @Override
        public E apply(@Nullable E input) {
            return copy(input);
        }
    };

    /**
     * Copy an element of the collection.
     *
     * @param elt The element to copy.
     * @return A copy of {@var elt}
     */
    protected abstract E copy(E elt);

    @Override
    public Iterator<E> iterator() {
        return Iterators.transform(fastIterator(), copyFunction);
    }
}
