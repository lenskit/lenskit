package org.lenskit.data.dao;

import org.junit.Test;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entity;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

/**
 * Tests for the entity query builder.  Some of its functionality is tested in {@link EntityQueryTest}, but this tests
 * additional builder features.
 */
public class EntityQueryBuilderTest {
    @Test
    public void testCopyEmpty() {
        EntityQueryBuilder eqb = EntityQuery.newBuilder();
        EntityQueryBuilder copy = eqb.copy();
        eqb.setEntityType(CommonTypes.ITEM);
        EntityQuery<Entity> q = copy.setEntityType(CommonTypes.USER).build();
        assertThat(q, notNullValue());
        assertThat(q.getEntityType(), equalTo(CommonTypes.USER));

        EntityQuery<Entity> q2 = eqb.build();
        assertThat(q2, notNullValue());
        assertThat(q2.getEntityType(), equalTo(CommonTypes.ITEM));
    }
}
