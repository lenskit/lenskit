/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.knn.user;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.grapht.annotation.DefaultImplementation;

/**
 * Finds candidate neighbors for a user.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 2.1
 */
@DefaultImplementation(LiveNeighborFinder.class)
public interface NeighborFinder {
    /**
     * Get potential neighbors for a particular user.
     *
     * @param user  The user ID whose neighbors are wanted.
     * @param items The items that the client needs to be able to score or recommend.
     * @return A collection of potential neighbors for {@code user}.  This collection may include
     *         neighbors that are not useful for scoring any item in {@code items}; the item set
     *         is just to help the neighbor finder guide its search if relevant.
     */
    Iterable<Neighbor> getCandidateNeighbors(long user, LongSet items);
}
