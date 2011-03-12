/*
 * Copyright 2010 Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 *
 */
package org.grouplens.lenskit.data;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;


import org.grouplens.common.cursors.Cursor;
import org.grouplens.common.cursors.Cursors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Additional cursor utility methods.
 * @see Cursors
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class Cursors2 {
    private static final Logger logger = LoggerFactory.getLogger(Cursors2.class);

    public static LongArrayList makeList(LongCursor cursor) {
        LongArrayList list = null;
        try {
            int n = cursor.getRowCount();
            if (n < 0) n = 10;
            list = new LongArrayList(n);
            while (cursor.hasNext()) {
                list.add(cursor.nextLong());
            }
        } catch (OutOfMemoryError e) {
            logger.error("Ran out of memory with {} users",
                    list == null ? -1 : list.size());
            throw e;
        } finally {
            cursor.close();
        }
        list.trim();
        return list;
    }

    public static LongCursor wrap(LongIterator iter) {
        return new LongIteratorCursor(iter);
    }

    public static LongCursor wrap(LongCollection collection) {
        return new LongCollectionCursor(collection);
    }

    public static <T> LongCursor makeLongCursor(final Cursor<Long> cursor) {
        if (cursor instanceof LongCursor)
            return (LongCursor) cursor;

        return new LongCursor() {
            public boolean hasNext() {
                return cursor.hasNext();
            }
            public Long next() {
                return cursor.next();
            }
            public long nextLong() {
                return next();
            }
            public void close() {
                cursor.close();
            }
            public int getRowCount() {
                return cursor.getRowCount();
            }
            @Override
            public LongIterator iterator() {
                return new LongCursorIterator(this);
            }
        };
    }
}
