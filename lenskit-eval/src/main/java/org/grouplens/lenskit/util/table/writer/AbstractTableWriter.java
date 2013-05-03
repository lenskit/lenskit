package org.grouplens.lenskit.util.table.writer;

import java.io.IOException;
import java.util.Arrays;

/**
 * Abstract helper class for implementing table writers.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 1.1
 */
public abstract class AbstractTableWriter implements TableWriter {
    /**
     * {@inheritDoc}
     * This implementation delegates to {@link #writeRow(java.util.List)}.
     */
    @Override
    public void writeRow(Object... row) throws IOException {
        writeRow(Arrays.asList(row));
    }

    /**
     * No-op close implementaiton.
     */
    @Override
    public void close() throws IOException {}
}
