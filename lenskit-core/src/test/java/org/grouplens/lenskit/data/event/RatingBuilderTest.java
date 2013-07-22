package org.grouplens.lenskit.data.event;

import org.grouplens.lenskit.data.pref.Preference;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class RatingBuilderTest {
    @Test
    public void testInitialState() {
        RatingBuilder rb = new RatingBuilder();
        assertThat(rb.hasRating(), equalTo(false));
        try {
            rb.build();
            fail("building a rating should fail");
        } catch (IllegalStateException e) {
            /* expected */
        }
    }

    @Test
    public void testInitialId() {
        RatingBuilder rb = new RatingBuilder(394);
        assertThat(rb.getId(), equalTo(394L));
    }

    @Test
    public void testSetId() {
        RatingBuilder rb = new RatingBuilder();
        rb.setId(10);
        assertThat(rb.getId(), equalTo(10L));
    }

    @Test
    public void testNewId() throws Exception {
        RatingBuilder rb = new RatingBuilder();
        rb.newId();
        long id = rb.getId();
        assertThat(id, greaterThan(0L));
        rb.newId();
        assertThat(rb.getId(), greaterThan(id));
    }

    @Test
    public void testSetUserId() {
        RatingBuilder rb = new RatingBuilder();
        rb.setUserId(42);
        assertThat(rb.getUserId(), equalTo(42L));
    }

    @Test
    public void testSetItemId() {
        RatingBuilder rb = new RatingBuilder();
        rb.setItemId(42);
        assertThat(rb.getItemId(), equalTo(42L));
    }

    @Test
    public void testSetRating() {
        RatingBuilder rb = new RatingBuilder();
        rb.setRating(3.5);
        assertThat(rb.hasRating(), equalTo(true));
        assertThat(rb.getRating(), equalTo(3.5));
    }

    @Test
    public void testClearRating() {
        RatingBuilder rb = new RatingBuilder();
        rb.setRating(3.5);
        rb.clearRating();
        assertThat(rb.hasRating(), equalTo(false));
    }

    @Test
    public void testSetTimestamp() {
        RatingBuilder rb = new RatingBuilder();
        rb.setTimestamp(235909);
        assertThat(rb.getTimestamp(), equalTo(235909L));
    }

    @Test
    public void testBuildRating() {
        Rating r = new RatingBuilder()
                .setId(39)
                .setUserId(692)
                .setItemId(483)
                .setRating(3.5)
                .setTimestamp(349702)
                .build();
        assertThat(r, notNullValue());
        assertThat(r.getId(), equalTo(39L));
        assertThat(r.getUserId(), equalTo(692L));
        assertThat(r.getItemId(), equalTo(483L));
        Preference pref = r.getPreference();
        assertThat(pref, notNullValue());
        assert pref != null;
        assertThat(pref.getValue(), equalTo(3.5));
        assertThat(pref.getUserId(), equalTo(692L));
        assertThat(pref.getItemId(), equalTo(483L));
        assertThat(r.getTimestamp(), equalTo(349702L));
    }

    @Test
    public void testBuildUnrate() {
        Rating r = new RatingBuilder()
                .setId(39)
                .setUserId(692)
                .setItemId(483)
                .setTimestamp(349702)
                .build();
        assertThat(r, notNullValue());
        assertThat(r.getId(), equalTo(39L));
        assertThat(r.getUserId(), equalTo(692L));
        assertThat(r.getItemId(), equalTo(483L));
        Preference pref = r.getPreference();
        assertThat(pref, nullValue());
        assertThat(r.getTimestamp(), equalTo(349702L));
    }
}
