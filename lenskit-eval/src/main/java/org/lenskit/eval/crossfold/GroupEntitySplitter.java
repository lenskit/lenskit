/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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
            return obj.getClass().equals(Partition.class);
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
