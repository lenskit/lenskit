/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.lenskit.data.dao;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.lenskit.data.dao.file.StaticFileDAOProvider;
import org.lenskit.data.entities.*;
import org.lenskit.util.io.ObjectStreams;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class StaticFileDAOProviderTest {
    private EntityFactory factory = new EntityFactory();

    @Test
    public void testSomeEvents() {
        StaticFileDAOProvider layout = new StaticFileDAOProvider();
        List<Entity> ratings = Lists.newArrayList(factory.rating(1L, 20L, 3.5),
                                                  factory.rating(1L, 21L, 4.5));
        layout.addSource(ratings);
        DataAccessObject dao = layout.get();
        assertThat(dao.getEntityTypes(), containsInAnyOrder(CommonTypes.RATING,
                                                            CommonTypes.USER,
                                                            CommonTypes.ITEM));
        assertThat(dao.lookupEntity(CommonTypes.RATING, ratings.get(0).getId()),
                   equalTo(ratings.get(0)));
        assertThat(ObjectStreams.makeList(dao.streamEntities(EntityQuery.newBuilder()
                                                                        .setEntityType(CommonTypes.RATING)
                                                                        .addFilterField(CommonAttributes.ITEM_ID, 20L)
                                                                        .build())),
                   contains(ratings.get(0)));

        assertThat(ObjectStreams.makeList(dao.streamEntities(EntityQuery.newBuilder()
                                                                        .setEntityType(CommonTypes.RATING)
                                                                        .addFilterField(CommonAttributes.USER_ID, 1L)
                                                                        .build())),
                   contains(ratings.toArray()));

        assertThat(dao.getEntityIds(CommonTypes.USER),
                   contains(1L));
        assertThat(dao.getEntityIds(CommonTypes.ITEM),
                   containsInAnyOrder(20L, 21L));
        assertThat(ObjectStreams.makeList(dao.streamEntities(CommonTypes.USER)),
                   contains(Entities.create(CommonTypes.USER, 1)));
        assertThat(ObjectStreams.makeList(dao.streamEntities(CommonTypes.ITEM)),
                   containsInAnyOrder(Entities.create(CommonTypes.ITEM, 20),
                                      Entities.create(CommonTypes.ITEM, 21)));
    }
}
