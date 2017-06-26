package org.lenskit.pf;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import org.apache.commons.math3.linear.RealVector;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.results.Results;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class HPFItemScorer extends AbstractItemScorer {
    private final HPFModel model;
    private final boolean isProbPredition;

    @Inject
    public HPFItemScorer(HPFModel mod,
                         @IsProbabilityPrediciton boolean probPred) {
        model = mod;
        isProbPredition = probPred;
    }

    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        RealVector uvec = model.getUserVector(user);
        if (uvec == null) {
            return Results.newResultMap();
        }

        List<Result> results = new ArrayList<>(items.size());
        LongIterator iter = LongIterators.asLongIterator(items.iterator());
        while (iter.hasNext()) {
            long item = iter.nextLong();
            RealVector ivec = model.getItemVector(item);
            if (ivec != null) {
                double score = uvec.dotProduct(ivec);
                if (isProbPredition) {
                    score = 1 - Math.exp(-score);
                }
                results.add(Results.create(item, score));
            }
        }
        return Results.newResultMap(results);
    }
}
