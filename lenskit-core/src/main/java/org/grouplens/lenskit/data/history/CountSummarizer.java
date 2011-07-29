package org.grouplens.lenskit.data.history;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;

import javax.annotation.Nonnull;

import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.vector.UserVector;

import com.google.common.collect.Iterables;

/**
 * Summarize a history by counting all events referencing an item.  The history
 * can be filtered by type prior to counting.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public final class CountSummarizer implements HistorySummarizer {
    protected final Class<? extends Event> wantedType;
    
    /**
     * Create a summarizer that counts all events.
     */
    public CountSummarizer() {
        this(Event.class);
    }
    
    /**
     * Create a summarizer that counts events of a particular type.
     * @param type
     */
    public CountSummarizer(@Nonnull Class<? extends Event> type) {
        wantedType = type;
    }

    @Override
    public UserVector summarize(UserHistory<? extends Event> history) {
        Long2DoubleMap map = new Long2DoubleOpenHashMap();
        for (Event e: Iterables.filter(history, wantedType)) {
            final long iid = e.getItemId();
            map.put(iid, map.get(iid) + 1);
        }
        return new UserVector(history.getUserId(), map);
    }
    
    @Override
    public int hashCode() {
        return wantedType.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof CountSummarizer) {
            CountSummarizer ocs = (CountSummarizer) o;
            return wantedType.equals(ocs.wantedType);
        } else {
            return false;
        }
    }
}
