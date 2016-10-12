/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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

import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.data.entities.CommonTypes;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Bridge to transition item DAO use to new DAOs.
 */
@SuppressWarnings("deprecation")
public class BridgeItemDAO implements ItemDAO {
    private final DataAccessObject delegate;

    /**
     * Construct a new bridge item DAO.
     * @param dao The underlying DAO.
     */
    @Inject
    public BridgeItemDAO(DataAccessObject dao) {
        delegate = dao;
    }

    @Override
    public LongSet getItemIds() {
        return delegate.getEntityIds(CommonTypes.ITEM);
    }

    public static class DynamicProvider implements Provider<ItemDAO> {
        private final DataAccessObject dao;
        private final EventDAO eventDao;

        @Inject
        public DynamicProvider(@Nullable DataAccessObject dao, @Nullable EventDAO events) {
            this.dao = dao;
            this.eventDao = events;
        }

        @Override
        public ItemDAO get() {
            if (dao != null) {
                return new BridgeItemDAO(dao);
            } else {
                return new PrefetchingItemDAO(eventDao);
            }
        }
    }
}
