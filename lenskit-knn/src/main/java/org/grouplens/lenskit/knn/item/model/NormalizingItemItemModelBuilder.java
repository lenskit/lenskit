package org.grouplens.lenskit.knn.item.model;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.knn.item.ItemSimilarity;
import org.grouplens.lenskit.knn.params.ModelSize;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.transform.normalize.ItemVectorNormalizer;
import org.grouplens.lenskit.transform.truncate.VectorTruncator;
import org.grouplens.lenskit.util.ScoredItemAccumulator;
import org.grouplens.lenskit.util.TopNScoredItemAccumulator;
import org.grouplens.lenskit.util.UnlimitedScoredItemAccumulator;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.util.List;

public class NormalizingItemItemModelBuilder implements Provider<ItemItemModel> {
    private static final Logger logger = LoggerFactory.getLogger(NormalizingItemItemModelBuilder.class);

    private final ItemSimilarity similarity;
    private final ItemItemBuildContextFactory contextFactory;
    private final int modelSize;
    private final ItemVectorNormalizer normalizer;
    private final VectorTruncator truncator;

    public NormalizingItemItemModelBuilder(ItemSimilarity similarity,
                                           @Transient ItemItemBuildContextFactory ctxFactory,
                                           ItemVectorNormalizer normalizer,
                                           VectorTruncator truncator,
                                           @ModelSize int modelSize) {
        this.similarity = similarity;
        contextFactory = ctxFactory;
        this.normalizer = normalizer;
        this.truncator = truncator;
        this.modelSize = modelSize;
    }


    @Override
    public ItemItemModel get() {
        logger.debug("building item-item model");

        ItemItemBuildContext context = contextFactory.buildContext();
        Accumulator accumulator = new Accumulator(context.getItems(), normalizer, truncator, modelSize);

        for (long itemId1 : context.getItems()) {
            for (long itemId2 : context.getItems()) {
                SparseVector vec1 = context.itemVector(itemId1);
                SparseVector vec2 = context.itemVector(itemId2);
                double sim = similarity.similarity(itemId1, vec1, itemId2, vec2);
                accumulator.put(itemId1, itemId2, sim);
            }
            accumulator.completeRow(itemId1);
        }

        return accumulator.build();
    }

    public static class Accumulator implements SimilarityMatrixAccumulator {

        private final LongSortedSet itemUniverse;
        private final ItemVectorNormalizer normalizer;
        private int modelSize;

        private Long2ObjectMap<MutableSparseVector> unfinishedRows;
        private Long2ObjectMap<ImmutableSparseVector> finishedRows;
        private VectorTruncator truncator;

        public Accumulator(LongSortedSet entities,
                           ItemVectorNormalizer normalizer,
                           VectorTruncator truncator,
                           int modelSize) {
            itemUniverse = entities;
            this.normalizer = normalizer;
            this.truncator = truncator;
            this.modelSize = modelSize;

            unfinishedRows = new Long2ObjectOpenHashMap<MutableSparseVector>(itemUniverse.size());
            finishedRows = new Long2ObjectOpenHashMap<ImmutableSparseVector>(itemUniverse.size());
            for (long itemId : itemUniverse) {
                unfinishedRows.put(itemId, new MutableSparseVector(itemUniverse));
            }
        }

        @Override
        public void put(long i, long j, double sim) {
            Preconditions.checkState(unfinishedRows != null, "model already built");

            // concurrent read-only array access permitted
            MutableSparseVector row = unfinishedRows.get(i);
            // synchronize on this row to add item
            synchronized (row) {
                row.set(j, sim);
            }
        }

        @Override
        public void completeRow(long rowId) {
            MutableSparseVector row = unfinishedRows.get(rowId);
            MutableSparseVector normalized = normalizer.normalize(rowId, row, null);
            if (truncator != null) {
                truncator.truncate(normalized);
            }

            finishedRows.put(rowId, normalized.freeze());
            unfinishedRows.remove(rowId);
        }

        @Override
        public SimilarityMatrixModel build() {
            Long2ObjectMap<ImmutableSparseVector> data =
                    new Long2ObjectOpenHashMap<ImmutableSparseVector>(finishedRows.size());
            ScoredItemAccumulator accum;
            if (modelSize > 0) {
                accum = new TopNScoredItemAccumulator(modelSize);
            } else {
                accum = new UnlimitedScoredItemAccumulator();
            }
            for (Long2ObjectMap.Entry<ImmutableSparseVector> row : finishedRows.long2ObjectEntrySet()) {
                ImmutableSparseVector rowVec = row.getValue();
                for (VectorEntry e : rowVec.fast()) {
                    accum.put(e.getKey(), e.getValue());
                }
                data.put(row.getLongKey(), accum.vectorFinish().freeze());
            }
            SimilarityMatrixModel model = new SimilarityMatrixModel(itemUniverse, data);
            unfinishedRows = null;
            finishedRows = null;
            return model;
        }
    }
}
