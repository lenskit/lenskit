package org.lenskit.mf.svdfeature;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.featurize.Entity;
import org.lenskit.results.Results;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureModelItemScorer extends AbstractItemScorer {
    final private SVDFeatureModel model;

    @Inject
    public SVDFeatureModelItemScorer(SVDFeatureModel model) {
        this.model = model;
    }

    public double predict(SVDFeatureInstance ins) {
        return model.predict(ins, true);
    }

    public List<SVDFeatureInstance> buildSVDFeatureInstance(long user, Collection<Long> items) {
        List<SVDFeatureInstance> instances = new ArrayList<>(items.size());
        for (Long item : items) {
            Entity entity = new Entity();
            //make userId and itemId configurable, currently it's a hard-coded string
            entity.setCatAttr("userId", Arrays.asList(Long.valueOf(user).toString()));
            entity.setCatAttr("itemId", Arrays.asList(item.toString()));
            instances.add((SVDFeatureInstance)(model.featurize(entity, false)));
        }
        return instances;
    }

    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        List<Result> scores = new ArrayList<>(items.size());
        List<SVDFeatureInstance> instances = buildSVDFeatureInstance(user, items);
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
