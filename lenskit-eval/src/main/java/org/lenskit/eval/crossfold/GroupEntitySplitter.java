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
package org.lenskit.eval.crossfold;

import com.google.common.primitives.Ints;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Split grouping entities.
 */
abstract class GroupEntitySplitter {
    private static final Logger logger = LoggerFactory.getLogger(GroupEntitySplitter.class);

    /**
     * Create a splitter that randomly partitions the entities.
     * @return A splitter that randomly partitions entities.
     */
    public static GroupEntitySplitter partition() {
        return new Partition();
    }

    /**
     * Create a splitter that computes disjoint samples.
     * @param size The sample size.
     * @return A splitter that computes disjoint samples of size {@code size}.
     */
    public static GroupEntitySplitter disjointSample(int size) {
        return new DisjointSample(size);
    }

    /**
     * Assign entities to partitions.
     * @param entities The entities to partition.
     * @param np The number of entity sets to build.
     * @param rng The random number generator.
     * @return A mapping of entities to their test partitions.
     */
    public abstract Long2IntMap splitEntities(LongSet entities, int np, Random rng);

    private static class Partition extends GroupEntitySplitter {
        @Override
        public Long2IntMap splitEntities(LongSet entities, int np, Random rng) {
            Long2IntMap emap = new Long2IntOpenHashMap(entities.size());
            logger.info("Splitting {} entities into {} partitions", entities.size(), np);
            long[] array = entities.toLongArray();
            LongArrays.shuffle(array, rng);
            for (int i = 0; i < array.length; i++) {
                final long user = array[i];
                emap.put(user, i % np);
            }
            return emap;
        }

        @Override
        public int hashCode() {
            return Partition.class.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && obj.getClass().equals(Partition.class);
        }

        @Override
        public String toString() {
            return "partition";
        }
    }

    private static class DisjointSample extends GroupEntitySplitter {
        private final int sampleSize;

        DisjointSample(int size) {
            sampleSize = size;
        }

        @Override
        public Long2IntMap splitEntities(LongSet entities, int np, Random rng) {
            if (np * sampleSize > entities.size()) {
                logger.warn("cannot make {} disjoint samples of {} from {} entities, partitioning",
                            np, sampleSize, entities.size());
                return partition().splitEntities(entities, np, rng);
            } else {
                Long2IntMap emap = new Long2IntOpenHashMap(entities.size());
                logger.info("Sampling {} entities into {} disjoint samples of {}",
                            entities.size(), np, sampleSize);
                long[] earray = entities.toLongArray();
                LongArrays.shuffle(earray, rng);
                for (int p = 0; p < np; p++) {
                    for (int i = 0; i < sampleSize; i++) {
                        long u = earray[p*sampleSize + i];
                        emap.put(u, p);
                    }
                }
                return emap;
            }
        }

        @Override
        public int hashCode() {
            return Ints.hashCode(sampleSize);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (obj == null || !(obj instanceof DisjointSample)) {
                return false;
            }

            DisjointSample that = (DisjointSample) obj;
            return sampleSize == that.sampleSize;
        }

        @Override
        public String toString() {
            return String.format("sample(%d)", sampleSize);
        }
    }
}
