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

package org.grouplens.lenskit.data.dao;

import javax.annotation.Nullable;

/**
 * A DAO interface that provides access to item names.
 * <p>
 * The normal way to get item names, without writing your own DAOs, is to use a {@link org.grouplens.lenskit.data.dao.MapItemNameDAO}, often
 * loaded from a CSV file:
 * </p>
 * <pre>{@code
 * bind MapItemNameDAO to CSVFileItemNameDAOProvider
 * set ItemFile to "item-names.csv"
 * }</pre>
 * <p>
 * Note that, while {@link org.grouplens.lenskit.data.dao.MapItemNameDAO} implements both this
 * interface and {@link org.grouplens.lenskit.data.dao.ItemDAO}, binding this interface to the
 * provider instead of the class means that the item name DAO will only be used to satisfy item name
 * DAO requests and not item list requests.
 * </p>
 */
public interface ItemNameDAO {
    /**
     * Get the name for an item.
     * @param item The item ID.
     * @return A display name for the item, or {@code null} if the item is unknown or has no name.
     */
    @Nullable
    String getItemName(long item);
}
