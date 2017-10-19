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
package org.lenskit.data.ratings;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.IntHeapPriorityQueue;
import org.apache.commons.lang3.builder.Builder;
import org.lenskit.util.keys.HashKeyIndex;

import java.util.Arrays;
import java.util.Random;

import static org.lenskit.data.ratings.PackedRatingData.*;

/**
 * Build a packed rating data structure.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.11
 */
class PackedRatingDataBuilder implements Builder<PackedRatingData> {
    private static final int INITIAL_CHUNK_COUNT = 512;

    private int[][] users;
    private int[][] items;
    private double[][] values;
    private int nprefs = 0;

    private HashKeyIndex itemIndex;
    private HashKeyIndex userIndex;
    
    private IntHeapPriorityQueue freeList;

    PackedRatingDataBuilder() {
        itemIndex = new HashKeyIndex();
        userIndex = new HashKeyIndex();
        freeList = new IntHeapPriorityQueue();
        allocate(INITIAL_CHUNK_COUNT);
    }

    /**
     * Allocate (or re-allocate) the internal packed storage.
     *
     * @param nchunks The number of chunks to have space for.
     */
    private void allocate(int nchunks) {
        if (users != null && nchunks == users.length) {
            return;
        }
        int[][] utmp = new int[nchunks][];
        int[][] itmp = new int[nchunks][];
        double[][] vtmp = new double[nchunks][];
        if (users != null) {
            assert items != null;
            assert values != null;
            int n = Math.min(nchunks, users.length);
            System.arraycopy(users, 0, utmp, 0, n);
            System.arraycopy(items, 0, itmp, 0, n);
            System.arraycopy(values, 0, vtmp, 0, n);
        }
        users = utmp;
        items = itmp;
        values = vtmp;
    }

    /**
     * Get the size of this data pack.
     *
     * @return The number of preferences in the data pack.
     */
    public int size() {
        return nprefs - freeList.size();
    }

    /**
     * Internal set method that takes individual indexes.
     *
     * @param ci   The chunk index.
     * @param ei   The element index.
     * @param user The user ID to set.
     * @param item The item ID to set.
     * @param pref The preference value to set.
     */
    private void set(int ci, int ei, long user, long item, double pref) {
        users[ci][ei] = userIndex.internId(user);
        items[ci][ei] = itemIndex.internId(item);
        values[ci][ei] = pref;
    }

    /**
     * Set the preference data at a particular index.
     *
     * @param idx  The index.
     * @param user The user ID to set.
     * @param item The item ID to set.
     * @param pref The preference value to set.
     */
    public void set(int idx, long user, long item, double pref) {
        Preconditions.checkElementIndex(idx, nprefs);
        final int ci = chunk(idx);
        final int ei = element(idx);
        set(ci, ei, user, item, pref);
    }

    /**
     * Add a rating to the pack.
     *
     * @param user The user ID to add.
     * @param item The item ID to add.
     * @param pref The preference value.
     * @return The index of the newly-added entry.
     */
    public int add(long user, long item, double pref) {
        assert users != null;
        assert items != null;
        assert values != null;
        assert users.length == items.length;
        assert values.length == users.length;

        final int idx = freeList.isEmpty() ? nprefs : freeList.dequeueInt();
        if (idx == Integer.MAX_VALUE) {
            throw new IllegalStateException("data pack full");
        }
        final int ci = chunk(idx);
        final int ei = element(idx);

        if (ci >= users.length) {
            // must resize
            allocate(users.length * 2);
        }
        if (users[ci] == null) {
            assert items[ci] == null;
            assert values[ci] == null;
            users[ci] = new int[CHUNK_SIZE];
            items[ci] = new int[CHUNK_SIZE];
            values[ci] = new double[CHUNK_SIZE];
        }

        set(ci, ei, user, item, pref);
        if (idx == nprefs) {
            nprefs += 1;
        }
        return idx;
    }

    private PackedRatingData internalBuild() {
        return new PackedRatingData(users, items, values, nprefs,
                                        userIndex.frozenCopy(),
                                        itemIndex.frozenCopy());
    }

    private void repack() {
        assert users.length == items.length;
        assert items.length == values.length;

        // if there are no free items, no reallocation is needed
        if (freeList.isEmpty()) {
            return;
        }

        // create an internal PRD so we can use preferences
        PackedRatingData tmpPack = internalBuild();
        // create an internal flyweight
        // after this point, this method does no allocation
        PackedRatingData.IndirectEntry pref = tmpPack.getEntry(-1);

        /*
         * we have to do this backwards so we don't copy free slots from the
         * end of the arrays. So, we first create an array of free indices
         * in reverse order (the greatest index is first).
         */
        int[] fidxes = new int[freeList.size()];
        for (int i = freeList.size() - 1; i >= 0; i--) {
            fidxes[i] = freeList.dequeueInt();
        }

        int n = nprefs;

        /*
         * We then start with the last free index, and copy into it if it
         * isn't at the end of the array.
         */
        for (int i: fidxes) {
            final int lasti = n - 1;    // the index of the last getEntry
            assert i <= lasti;          // only way for this to fail is duplicate fidxes
            if (i < lasti) {
                // if it is not the last element, move the last to it
                pref.setIndex(lasti);
                set(i, pref.getUserId(), pref.getItemId(), pref.getValue());
            }
            // finally, we decrease our count by 1
            n -= 1;
        }

        // Now that we have used all free indexes, we can update nprefs
        assert nprefs == n + fidxes.length;
        assert freeList.isEmpty();
        nprefs = n;
    }

    private void swap(int i, int j) {
        if (i == j) {
            return;
        }

        int ci = chunk(i);
        int ei = element(i);
        int cj = chunk(j);
        int ej = element(j);

        int tidx;
        double tv;

        tidx = users[ci][ei];
        users[ci][ei] = users[cj][ej];
        users[cj][ej] = tidx;

        tidx = items[ci][ei];
        items[ci][ei] = items[cj][ej];
        items[cj][ej] = tidx;

        tv = values[ci][ei];
        values[ci][ei] = values[cj][ej];
        values[cj][ej] = tv;
    }

    /**
     * Shuffle the data. This uses a Fischer-Yates shuffle to uniformly permute
     * (subject to limitations of the PRNG) the data. The arrays are repacked
     * to eliminate free slots prior to shuffling.
     *
     * @param rng The random number generator to use.
     */
    void shuffle(Random rng) {
        repack();
        // do a reverse Fisher-Yates shuffle on the arrays
        final int np = nprefs;
        for (int i = 0; i < np - 1; i++) {
            // swap w/ j s.t. i <= j < end
            int j = i + rng.nextInt(np - i);
            assert j >= i;
            assert j < np;
            swap(i, j);
        }
    }

    /**
     * Trim the arrays.
     */
    private void trim() {
        // shortcut out if nprefs == 0
        if (nprefs == 0) {
            allocate(0);
            return;
        }

        // how many chunks? and how many in last?
        // but be careful to avoid integer overflow
        int nchunks = nprefs >> CHUNK_SHIFT;
        int nlast = nprefs & CHUNK_MASK;
        if (nlast == 0) {
            // we we can't have 0 - we must have CHUNK_SIZE
            nlast = CHUNK_SIZE;
        } else {
            // then we undercounted the number of chunks
            nchunks += 1;
        }
        assert nchunks * CHUNK_SIZE - CHUNK_SIZE + nlast == nprefs;

        // trim chunks
        allocate(nchunks);

        // trim last element
        if (nlast < CHUNK_SIZE) {
            final int lci = nchunks - 1;
            users[lci] = Arrays.copyOf(users[lci], nlast);
            items[lci] = Arrays.copyOf(items[lci], nlast);
            values[lci] = Arrays.copyOf(values[lci], nlast);
        }
    }

    /**
     * Build the packed rating data. This first moves records from the end to fill
     * any released but not reused slots.
     *
     * @return The packed rating data structure.
     */
    @Override
    public PackedRatingData build() {
        repack();
        trim();
        return internalBuild();
    }
}
