package org.lenskit.data.summary;

import org.junit.Test;
import org.lenskit.util.keys.KeyedObjectMap;

import static org.grouplens.lenskit.util.test.ExtraMatchers.notANumber;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class RatingSummaryTest {
    @Test
    public void testEmptySummary() {
        KeyedObjectMap<RatingSummary.ItemSummary> items =
                KeyedObjectMap.<RatingSummary.ItemSummary>newBuilder()
                              .build();
        RatingSummary sum = new RatingSummary(3.5, items);
        assertThat(sum.getGlobalMean(), equalTo(3.5));
        assertThat(sum.getItemMean(42), notANumber());
        assertThat(sum.getItemOffset(42), equalTo(0.0));
        assertThat(sum.getItemRatingCount(42), equalTo(0));
    }

    @Test
    public void testSummaryItem() {
        RatingSummary.ItemSummary item = new RatingSummary.ItemSummary(37, 3.9, 100);
        KeyedObjectMap<RatingSummary.ItemSummary> items =
                KeyedObjectMap.<RatingSummary.ItemSummary>newBuilder()
                              .add(item)
                              .build();
        RatingSummary sum = new RatingSummary(3.5, items);
        assertThat(sum.getGlobalMean(), equalTo(3.5));
        assertThat(sum.getItemMean(42), notANumber());
        assertThat(sum.getItemOffset(42), equalTo(0.0));
        assertThat(sum.getItemRatingCount(42), equalTo(0));
        assertThat(sum.getItemMean(37), equalTo(3.9));
        assertThat(sum.getItemOffset(37), closeTo(0.4, 1.0e-6));
        assertThat(sum.getItemRatingCount(37), equalTo(100));
    }
}
