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

import org.junit.Test;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entities;
import org.lenskit.util.io.ObjectStreams;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class EntityCollectionDAOTest {
    @Test
    public void testEmptyDAO() {
        EntityCollectionDAO dao = EntityCollectionDAO.create();
        assertThat(dao.getEntityIds(CommonTypes.ITEM), hasSize(0));
        assertThat(dao.getEntityIds(CommonTypes.RATING), hasSize(0));
        assertThat(ObjectStreams.makeList(dao.streamEntities(CommonTypes.RATING)),
                   hasSize(0));
    }

    @Test
    public void testOneEntity() {
        EntityCollectionDAO dao = EntityCollectionDAO.create(Entities.create(CommonTypes.USER, 42));
        assertThat(dao.getEntityIds(CommonTypes.ITEM), hasSize(0));
        assertThat(dao.getEntityIds(CommonTypes.USER),
                   contains(42L));
        assertThat(ObjectStreams.makeList(dao.streamEntities(CommonTypes.RATING)),
                   hasSize(0));
        assertThat(ObjectStreams.makeList(dao.streamEntities(CommonTypes.USER)),
                   contains(Entities.create(CommonTypes.USER, 42L)));
        assertThat(dao.lookupEntity(CommonTypes.USER, 42),
                   equalTo(Entities.create(CommonTypes.USER, 42)));
    }
}
