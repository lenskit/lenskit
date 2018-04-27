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
package org.lenskit.knn.item.model;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.longs.*;
import net.jcip.annotations.NotThreadSafe;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.lenskit.inject.Transient;
import org.lenskit.knn.item.ItemSimilarity;
import org.lenskit.knn.item.ItemSimilarityThreshold;
import org.lenskit.knn.item.MinCommonUsers;
import org.lenskit.knn.item.ModelSize;
import org.lenskit.util.IdBox;
import org.lenskit.util.ProgressLogger;
import org.lenskit.util.collections.Long2DoubleAccumulator;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.collections.TopNLong2DoubleAccumulator;
import org.lenskit.util.collections.UnlimitedLong2DoubleAccumulator;
import org.lenskit.util.reflect.ClassQueries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Build an item-item CF model from rating data.
 * This builder takes a very simple approach. It does not allow for vector
 * normalization and truncates on the fly.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@NotThreadSafe
public class ItemItemModelProvider implements Provider<ItemItemModel> {
    private static final Logger logger = LoggerFactory.getLogger(ItemItemModelProvider.class);

    private final ItemSimilarity itemSimilarity;
    private final ItemItemBuildContext buildContext;
    private final Threshold threshold;
    private final NeighborIterationStrategy neighborStrategy;
    private final int minCommonUsers;
    private final int modelSize;

    @Inject
    public ItemItemModelProvider(@Transient ItemSimilarity similarity,
                                 @Transient ItemItemBuildContext context,
                                 @Transient @ItemSimilarityThreshold Threshold thresh,
                                 @Transient NeighborIterationStrategy nbrStrat,
                                 @MinCommonUsers int minCU,
                                 @ModelSize int size) {
        itemSimilarity = similarity;
        buildContext = context;
        threshold = thresh;
        neighborStrategy = nbrStrat;
        minCommonUsers = minCU;
        modelSize = size;
    }

    @Override
    public SimilarityMatrixModel get() {
        logger.info("building item-item model for {} items", buildContext.getItems().size());
        logger.debug("using similarity function {}", itemSimilarity);
        logger.debug("similarity function is {}",
                     itemSimilarity.isSparse() ? "sparse" : "non-sparse");
        logger.debug("similarity function is {}",
                     itemSimilarity.isSymmetric() ? "symmetric" : "non-symmetric");

        LongSortedSet allItems = buildContext.getItems();

        final int nitems = allItems.size();

        ProgressLogger progress = ProgressLogger.create(logger)
                                                .setCount(nitems)
                                                .setLabel("item-item model build")
                                                .setWindow(50)
                                                .start();
        int ndone = 0;
        Stream<Long> idStream;
        if (ClassQueries.isThreadSafe(itemSimilarity)) {
            idStream = allItems.parallelStream();
        } else {
            logger.warn("similarity {} is not thread-safe, disabling parallel build", itemSimilarity);
            idStream = allItems.stream();
        }
        Stream<IdBox<Long2DoubleMap>> rowStream =
                idStream.map(i -> IdBox.create(i, buildContext.itemVector(i)))
                        .peek(iv -> {
                            if (logger.isTraceEnabled()) {
                                logger.trace("computing similarities for item {}", iv.getId());
                            }
                        })
                        .filter(iv -> iv.getValue().size() >= minCommonUsers)
                        .map(this::makeSimilarityRow)
                        .peek(iv -> progress.advance());
        Long2ObjectMap<Long2DoubleMap> sims;
        if (itemSimilarity.isSymmetric()) {
            logger.info("using symmetric similarity collector");
            sims = rowStream.collect(new SymmetricCollector());
        } else {
            logger.info("using asymmteric similarity collector");
            sims = rowStream.collect(new BasicCollector());
        }

        progress.finish();
        logger.info("built model for {} items in {}",
                    ndone, progress.elapsedTime());

        return new SimilarityMatrixModel(sims);
    }

    private IdBox<Long2DoubleMap> makeSimilarityRow(IdBox<Long2DoubleSortedMap> item) {
        long itemId1 = item.getId();
        LongIterator itemIter = neighborStrategy.neighborIterator(buildContext, itemId1,
                                                                  itemSimilarity.isSymmetric());
        Long2DoubleSortedMap vec1 = item.getValue();
        Long2DoubleMap row = new Long2DoubleOpenHashMap();

        while (itemIter.hasNext()) {
            long itemId2 = itemIter.nextLong();
            if (itemId1 != itemId2) {
                Long2DoubleSortedMap vec2 = buildContext.itemVector(itemId2);
                if (!LongUtils.hasNCommonItems(vec1.keySet(), vec2.keySet(), minCommonUsers)) {
                    // items have insufficient users in common, skip them
                    continue;
                }

                double sim = itemSimilarity.similarity(itemId1, vec1, itemId2, vec2);
                if (threshold.retain(sim)) {
                    row.put(itemId2, sim);
                }
            }
        }

        return IdBox.create(itemId1, row);
    }

    @Nonnull
    private Long2DoubleAccumulator newAccumulator() {
        Long2DoubleAccumulator accum;
        if (modelSize <= 0) {
            accum = new UnlimitedLong2DoubleAccumulator();
        } else {
            accum = new TopNLong2DoubleAccumulator(modelSize);
        }
        return accum;
    }

    private Long2ObjectMap<Long2DoubleMap> finishRows(Long2ObjectMap<Long2DoubleAccumulator> rows) {
        Long2ObjectMap<Long2DoubleMap> results = new Long2ObjectOpenHashMap<>(rows.size());
        for (Long2ObjectMap.Entry<Long2DoubleAccumulator> e: rows.long2ObjectEntrySet()) {
            results.put(e.getLongKey(), e.getValue().finishMap());
        }
        return results;
    }

    private class BasicCollector implements Collector<IdBox<Long2DoubleMap>, Map<Long,Long2DoubleMap>, Long2ObjectMap<Long2DoubleMap>> {
        @Override
        public Supplier<Map<Long, Long2DoubleMap>> supplier() {
            return ConcurrentHashMap::new;
        }

        @Override
        public Function<Map<Long, Long2DoubleMap>, Long2ObjectMap<Long2DoubleMap>> finisher() {
            return Long2ObjectArrayMap::new;
        }

        @Override
        public BiConsumer<Map<Long, Long2DoubleMap>, IdBox<Long2DoubleMap>> accumulator() {
            return (acc, row) -> {
                Long2DoubleMap r2;
                if (modelSize <= 0) {
                    r2 = LongUtils.frozenMap(row.getValue());
                } else {
                    Long2DoubleAccumulator racc = new TopNLong2DoubleAccumulator(modelSize);
                    racc.putAll(row.getValue());
                    r2 = racc.finishMap();
                }
                Long2DoubleMap res = acc.putIfAbsent(row.getId(), r2);
                assert res == null;
            };
        }

        @Override
        public BinaryOperator<Map<Long, Long2DoubleMap>> combiner() {
            return (acc1, acc2) -> {
                for (Map.Entry<Long, Long2DoubleMap> e: acc2.entrySet()) {
                    Long2DoubleMap old = acc1.putIfAbsent(e.getKey(), e.getValue());
                    assert old == null;
                }
                return acc1;
            };
        }

        @Override
        public Set<Characteristics> characteristics() {
            return ImmutableSet.of(Characteristics.UNORDERED, Characteristics.CONCURRENT);
        }
    }

    private class SymmetricCollector implements Collector<IdBox<Long2DoubleMap>, Long2ObjectMap<Long2DoubleAccumulator>, Long2ObjectMap<Long2DoubleMap>> {
        @Override
        public Supplier<Long2ObjectMap<Long2DoubleAccumulator>> supplier() {
            return Long2ObjectArrayMap::new;
        }

        @Override
        public BiConsumer<Long2ObjectMap<Long2DoubleAccumulator>, IdBox<Long2DoubleMap>> accumulator() {
            return (acc, row) -> {
                long i1 = row.getId();
                for (Long2DoubleMap.Entry e: Long2DoubleMaps.fastIterable(row.getValue())) {
                    long i2 = e.getLongKey();
                    double sim = e.getDoubleValue();
                    acc.computeIfAbsent(i1, i -> newAccumulator())
                       .put(i2, sim);
                    acc.computeIfAbsent(i2, i -> newAccumulator())
                       .put(i1, sim);
                }
            };
        }

        @Override
        public BinaryOperator<Long2ObjectMap<Long2DoubleAccumulator>> combiner() {
            return (am1, am2) -> {
                for (Long2ObjectMap.Entry<Long2DoubleAccumulator> e: Long2ObjectMaps.fastIterable(am2)) {
                    long item = e.getLongKey();
                    Long2DoubleAccumulator a2 = e.getValue();
                    Long2DoubleAccumulator a1 = am1.get(item);
                    if (a1 == null) {
                        am1.put(item, a2);
                    } else {
                        for (Long2DoubleMap.Entry ae: Long2DoubleMaps.fastIterable(a2.finishMap())) {
                            a1.put(ae.getLongKey(), ae.getDoubleValue());
                        }
                    }
                }
                return am1;
            };
        }

        @Override
        public Function<Long2ObjectMap<Long2DoubleAccumulator>, Long2ObjectMap<Long2DoubleMap>> finisher() {
            return ItemItemModelProvider.this::finishRows;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return ImmutableSet.of(Characteristics.UNORDERED);
        }
    }
}
