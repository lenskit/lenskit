package org.lenskit.slim;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.inject.Shareable;
import org.lenskit.util.collections.LongUtils;

import java.io.Serializable;

/**
 * SLIM model
 * implement paper SLIM: Sparse Linear Methods for Top-N Recommender Systems
 */
@DefaultProvider(SlimModelProvider.class)
@Shareable
public class SlimModel implements Serializable {
    private static final long serialVersionUID = 3L;

    private final Long2ObjectMap<Long2DoubleMap> trainedWeights;

    public SlimModel(Long2ObjectMap<Long2DoubleMap> weights) {
        trainedWeights = weights;
    }

    public Long2DoubleMap getWeights(long item) {
        return LongUtils.frozenMap(trainedWeights.get(item));
    }
}
