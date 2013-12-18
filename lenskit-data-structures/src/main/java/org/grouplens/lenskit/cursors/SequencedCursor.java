package org.grouplens.lenskit.cursors;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implementation of concatenated cursors.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class SequencedCursor<T> extends AbstractCursor<T> {
    private final Iterator<? extends Cursor<? extends T>> cursorIter;
    @Nullable
    private Cursor<? extends T> current;

    public SequencedCursor(Iterable<? extends Cursor<? extends T>> cursors) {
        cursorIter = cursors.iterator();
        current = cursorIter.hasNext() ? cursorIter.next() : null;
    }

    @Override
    public boolean hasNext() {
        // advance
        while (current != null && !current.hasNext()) {
            current.close();
            if (cursorIter.hasNext()) {
                current = cursorIter.next();
                Preconditions.checkNotNull(current, "concatenated cursor");
            } else {
                current = null;
            }
        }
        assert current == null || current.hasNext();
        return current != null;
    }

    @Nonnull
    @Override
    public T next() {
        if (hasNext()) {
            assert current != null && current.hasNext();
            return current.next();
        } else {
            throw new NoSuchElementException();
        }
    }

    @Nonnull
    @Override
    public T fastNext() {
        if (hasNext()) {
            assert current != null && current.hasNext();
            return current.fastNext();
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public void close() {
        Throwable error = null;
        if (current != null) {
            try {
                current.close();
            } catch (Throwable th) {
                error = th;
            }
            current = null;
        }
        while (cursorIter.hasNext()) {
            Cursor<? extends T> cur = cursorIter.next();
            try {
                cur.close();
            } catch (Throwable th) {
                if (error != null) {
                    error = th;
                }
            }
        }
        if (error != null) {
            throw Throwables.propagate(error);
        }
    }
}
