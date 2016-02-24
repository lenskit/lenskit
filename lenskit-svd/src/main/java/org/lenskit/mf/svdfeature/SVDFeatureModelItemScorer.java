package org.lenskit.mf.svdfeature;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.results.Results;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureModelItemScorer extends AbstractItemScorer {
    private SVDFeatureModel model;

    @Inject
    public SVDFeatureModelItemScorer(SVDFeatureModel inModel) {
        model = inModel;
    }

    public double predict(SVDFeatureInstance ins) {
        return model.predict(ins, true);
    }

    public ArrayList<SVDFeatureInstance> buildSVDFeatureInstance(long user, Collection<Long> items) {
        ArrayList<SVDFeatureInstance> instances = new ArrayList<>(items.size());
        //TODO: id 2 index mapping
        return instances;
    }

    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        List<Result> scores = new ArrayList<>(items.size());
        ArrayList<SVDFeatureInstance> instances = buildSVDFeatureInstance(user, items);

        LongIterator iter = LongIterators.asLongIterator(items.iterator());
        int i = 0;
        while (iter.hasNext()) {
            long item = iter.nextLong();
            double score = predict(instances.get(i));
            scores.add(Results.create(item, score));
            i++;
        }
        return Results.newResultMap(scores);
    }
}
