package org.grouplens.lenskit.data.history;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.SimpleRating;
import org.grouplens.lenskit.vectors.SparseVector;
import org.junit.Test;
import org.omg.CORBA.LongSeqHelper;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class BasicUserHistoryTest {
    @Test
    public void testEmptyList() {
        UserHistory<Event> history = History.forUser(42, ImmutableList.<Event>of());
        assertThat(history.size(), equalTo(0));
        assertThat(history.isEmpty(), equalTo(true));
        assertThat(history.getUserId(), equalTo(42L));
    }

    @Test
    public void testSingletonList() {
        Rating r = new SimpleRating(1, 42, 39, 2.5);
        UserHistory<Rating> history = History.forUser(42, ImmutableList.of(r));
        assertThat(history.size(), equalTo(1));
        assertThat(history.isEmpty(), equalTo(false));
        assertThat(history.getUserId(), equalTo(42L));
        assertThat(history, contains(r));
    }

    @Test
    public void testMemoize() {
        List<Event> events = Lists.newArrayList();
        events.add(new SimpleRating(1, 42, 39, 2.5));
        events.add(new SimpleRating(1, 42, 62, 3.5));
        events.add(new SimpleRating(1, 42, 22, 3));
        UserHistory<Event> history = History.forUser(42, events);
        assertThat(history, hasSize(3));
        SparseVector v = history.memoize(RatingVectorUserHistorySummarizer.SummaryFunction.INSTANCE);
        assertThat(v.size(), equalTo(3));
        assertThat(v.mean(), equalTo(3.0));
        assertThat(history.memoize(RatingVectorUserHistorySummarizer.SummaryFunction.INSTANCE),
                   sameInstance(v));
    }

    @Test
    public void testIdSet() {
        List<Event> events = Lists.newArrayList();
        events.add(new SimpleRating(1, 42, 39, 2.5));
        events.add(new SimpleRating(1, 42, 62, 3.5));
        events.add(new SimpleRating(1, 42, 22, 3));
        UserHistory<Event> history = History.forUser(42, events);
        assertThat(history, hasSize(3));
        LongSet ids = history.itemSet();
        assertThat(ids, hasSize(3));
        assertThat(ids, containsInAnyOrder(39L, 62L, 22L));
    }
}
