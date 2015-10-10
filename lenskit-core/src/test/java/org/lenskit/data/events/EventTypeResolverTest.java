package org.lenskit.data.events;

import org.junit.Before;
import org.junit.Test;
import org.lenskit.data.ratings.RatingBuilder;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class EventTypeResolverTest {
    EventTypeResolver resolver;

    @Before
    public void createResolver() {
        resolver = EventTypeResolver.create();
    }

    @Test
    public void testGetRatingBuilder() {
        EventBuilder<?> eb = resolver.getEventBuilder("rating");
        assertThat(eb, instanceOf(RatingBuilder.class));
    }

    @Test
    public void testGetLikeBuilder() {
        EventBuilder<?> eb = resolver.getEventBuilder("like");
        assertThat(eb, instanceOf(LikeBuilder.class));
    }

    @Test
    public void testGetLikeBatchBuilder() {
        EventBuilder<?> eb = resolver.getEventBuilder("like-batch");
        assertThat(eb, instanceOf(LikeBatchBuilder.class));
    }

    @Test
    public void testGetLikeBatchBuilderFromClass() {
        EventBuilder<LikeBatch> eb = resolver.getEventBuilder(LikeBatch.class);
        assertThat(eb, instanceOf(LikeBatchBuilder.class));
    }

    @Test
    public void testGetNonexistentBuilder() {
        EventBuilder<?> eb = resolver.getEventBuilder("foobar");
        assertThat(eb, nullValue());
    }
}
