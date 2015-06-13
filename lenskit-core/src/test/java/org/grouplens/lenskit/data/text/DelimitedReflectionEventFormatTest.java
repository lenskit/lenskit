package org.grouplens.lenskit.data.text;

import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.RatingBuilder;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

public class DelimitedReflectionEventFormatTest {
    @Test
    public void testBasicParse() throws InvalidRowException {
        DelimitedReflectionEventFormat fmt = DelimitedReflectionEventFormat.create(",", RatingBuilder.class, "userId", "itemId", "rating");
        Event evt = fmt.parse("42,3,2.5");
        assertThat(evt, instanceOf(Rating.class));
        Rating r = (Rating) evt;
        assertThat(r.getUserId(), equalTo(42L));
        assertThat(r.getItemId(), equalTo(3L));
        assertThat(r.getValue(), equalTo(2.5));
        assertThat(r.getTimestamp(), equalTo(-1L));
    }

    @Test
    public void testParseWithTimestamp() throws InvalidRowException {
        DelimitedReflectionEventFormat fmt = DelimitedReflectionEventFormat.create(",", RatingBuilder.class, "userId", "itemId", "rating", "timestamp");
        Event evt = fmt.parse("42,3,2.5,838");
        assertThat(evt, instanceOf(Rating.class));
        Rating r = (Rating) evt;
        assertThat(r.getUserId(), equalTo(42L));
        assertThat(r.getItemId(), equalTo(3L));
        assertThat(r.getValue(), equalTo(2.5));
        assertThat(r.getTimestamp(), equalTo(838L));
    }
}