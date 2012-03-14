/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
/**
 *
 */
package org.grouplens.lenskit.cursors;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;

import javax.annotation.WillClose;

/**
 * Old home for primitive cursor methods.
 * @see Cursors
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@Deprecated
public class Cursors2 {
    @Deprecated
    public static LongArrayList makeList(@WillClose LongCursor cursor) {
        return Cursors.makeList(cursor);
    }

    @Deprecated
    public static LongSet makeSet(@WillClose LongCursor cursor) {
        return Cursors.makeSet(cursor);
    }

    @Deprecated
    public static LongCursor wrap(LongIterator iter) {
        return new LongIteratorCursor(iter);
    }
    
    @Deprecated
    public static LongCursor wrap(LongCollection collection) {
        return new LongCollectionCursor(collection);
    }

    @Deprecated
    public static <T> LongCursor makeLongCursor(final Cursor<Long> cursor) {
        return Cursors.makeLongCursor(cursor);
    }
}
