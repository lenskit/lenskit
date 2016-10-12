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

import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entity;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Bridge implementation of the item name DAO.
 */
public class BridgeItemNameDAO implements ItemNameDAO {
    private final DataAccessObject delegate;

    /**
     * Construct a new DAO.
     * @param dao The underlying DAO.
     */
    @Inject
    public BridgeItemNameDAO(DataAccessObject dao) {
        delegate = dao;
    }

    @Nullable
    @Override
    public String getItemName(long id) {
        Entity item = delegate.lookupEntity(CommonTypes.ITEM, id);
        if (item == null) {
            return null;
        } else {
            return item.maybeGet(CommonAttributes.NAME);
        }
    }
}
