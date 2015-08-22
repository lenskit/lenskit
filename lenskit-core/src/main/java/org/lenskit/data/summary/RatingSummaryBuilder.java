package org.lenskit.data.summary;

import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.ItemEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.data.history.ItemEventCollection;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.lenskit.util.keys.KeyedObjectMap;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

/**
 * Default builder for rating summaries.  This builder is correct, but may be inefficient.  If your data set does not
 * have any unrate events, consider using {@link FastRatingSummaryBuilder}:
 *
 * ```java
 * config.bind(RatingSummary.class)
 *       .toProvider(FastRatingSummaryBuilder.class);
 * ```
 *
 * @since 3.0
 */
public class RatingSummaryBuilder implements Provider<RatingSummary> {
    private final ItemEventDAO itemEventDAO;

    @Inject
    public RatingSummaryBuilder(@Transient ItemEventDAO dao) {
        itemEventDAO = dao;
    }

    @Override
    public RatingSummary get() {
        double totalSum = 0;
        int totalCount = 0;
        List<RatingSummary.ItemSummary> summaries = new ArrayList<>();

        try (Cursor<ItemEventCollection<Rating>> ratings = itemEventDAO.streamEventsByItem(Rating.class)) {
            for (ItemEventCollection<Rating> item: ratings) {
                MutableSparseVector vec = Ratings.itemRatingVector(item);
                int n = vec.size();
                double sum = vec.sum();
                double mean = vec.mean();
                totalSum += sum;
                totalCount += n;
                summaries.add(new RatingSummary.ItemSummary(item.getItemId(), mean, n));
            }
        }

        return new RatingSummary(totalSum / totalCount, KeyedObjectMap.create(summaries));
    }
}
