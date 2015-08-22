package org.lenskit.data.summary;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.lenskit.util.keys.KeyedObjectMap;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

/**
 * An efficient rating summary builder that does not acknowledge rerate/unrate events.
 *
 * @since 3.0
 */
public class FastRatingSummaryBuilder implements Provider<RatingSummary> {
    private final EventDAO eventDAO;

    @Inject
    public FastRatingSummaryBuilder(@Transient EventDAO dao) {
        eventDAO = dao;
    }

    @Override
    public RatingSummary get() {
        Long2DoubleMap sums = new Long2DoubleOpenHashMap();
        Long2IntMap counts = new Long2IntOpenHashMap();
        double totalSum = 0;
        int totalCount = 0;

        try (Cursor<Rating> ratings = eventDAO.streamEvents(Rating.class)) {
            for (Rating r: ratings) {
                if (r.hasValue()) {
                    long item = r.getItemId();
                    counts.put(item, counts.get(item) + 1);
                    sums.put(item, sums.get(item) + r.getValue());
                    totalSum += r.getValue();
                    totalCount += 1;
                }
            }
        }

        List<RatingSummary.ItemSummary> summaries = new ArrayList<>(sums.size());

        for (Long2DoubleMap.Entry e: sums.long2DoubleEntrySet()) {
            long item = e.getLongKey();
            double sum = e.getDoubleValue();
            int count = counts.get(item);
            summaries.add(new RatingSummary.ItemSummary(item, sum / count, count));
        }
        return new RatingSummary(totalSum / totalCount, KeyedObjectMap.create(summaries));
    }
}
