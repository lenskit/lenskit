package org.lenskit.mf.svdfeature;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.data.entities.*;
import org.lenskit.results.Results;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureItemScorer extends AbstractItemScorer {
    final private SVDFeatureModel model;

    @Inject
    public SVDFeatureItemScorer(SVDFeatureModel model) {
        this.model = model;
    }

    public double predict(SVDFeatureInstance ins) {
        return model.predict(ins, false);
    }

    public List<SVDFeatureInstance> buildSVDFeatureInstance(long user, Collection<Long> items) {
        List<SVDFeatureInstance> instances = new ArrayList<>(items.size());
        for (Long item : items) {
            BasicEntityBuilder builder = new BasicEntityBuilder(CommonTypes.RATING);
            builder.setAttribute(CommonAttributes.USER_ID, user);
            builder.setAttribute(CommonAttributes.ITEM_ID, item);
            builder.setAttribute(CommonAttributes.ENTITY_ID, item);
            builder.setAttribute(CommonAttributes.RATING, 0.0);
            instances.add((SVDFeatureInstance)(model.featurize(builder.build(), false)));
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
