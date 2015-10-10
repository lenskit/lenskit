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
        Class<? extends EventBuilder> eb = resolver.getEventBuilder("rating");
        assertThat(eb, equalTo((Class) RatingBuilder.class));
    }

    @Test
    public void testGetLikeBuilder() {
        Class<? extends EventBuilder> eb = resolver.getEventBuilder("like");
        assertThat(eb, equalTo((Class) LikeBuilder.class));
    }

    @Test
    public void testGetLikeBatchBuilder() {
        Class<? extends EventBuilder> eb = resolver.getEventBuilder("like-batch");
        assertThat(eb, equalTo((Class) LikeBatchBuilder.class));
    }

    @Test
    public void testGetLikeBatchBuilderFromClass() {
        Class<? extends EventBuilder> eb = resolver.getEventBuilder(LikeBatch.class);
        assertThat(eb, equalTo((Class) LikeBatchBuilder.class));
    }

    @Test
    public void testGetNonexistentBuilder() {
        Class<? extends EventBuilder> eb = resolver.getEventBuilder("foobar");
        assertThat(eb, nullValue());
    }
}
