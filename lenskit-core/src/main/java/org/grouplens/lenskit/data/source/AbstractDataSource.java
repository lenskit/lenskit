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
package org.grouplens.lenskit.data.source;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.data.dao.*;
import org.lenskit.data.ratings.PreferenceDomain;
import org.grouplens.lenskit.util.MoreSuppliers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class to help implement data sources. This class automatically wraps the event DAO in a
 * prefetching DAO to produce the specialized DAO classes, if it does not already implement those
 * classes. It also produces a LensKit configuration with the appropriate bindings for the
 * configured data source.
 *
 * @since 2.2
 */
public abstract class AbstractDataSource implements DataSource {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Supplier<EventDAO> eventDAOSupplier = new Supplier<EventDAO>() {
        @Override
        public EventDAO get() {
            return getEventDAO();
        }
    };
    private final Supplier<UserDAO> userDAOCache =
            MoreSuppliers.softMemoize(Suppliers.compose(PrefetchingUserDAO.wrapper(),
                                                        eventDAOSupplier));
    private final Supplier<UserEventDAO> userEventDAOCache =
            MoreSuppliers.softMemoize(Suppliers.compose(PrefetchingUserEventDAO.wrapper(),
                                                        eventDAOSupplier));
    private final Supplier<ItemDAO> itemDAOCache =
            MoreSuppliers.softMemoize(Suppliers.compose(PrefetchingItemDAO.wrapper(),
                                                        eventDAOSupplier));
    private final Supplier<ItemEventDAO> itemEventDAOCache =
            MoreSuppliers.softMemoize(Suppliers.compose(PrefetchingItemEventDAO.wrapper(),
                                                        eventDAOSupplier));

    /**
     * Default user-event DAO implementation.  If the {@linkplain #getEventDAO() event DAO}
     * implements {@link UserEventDAO}, it is returned directly; otherwise, a new {@link PrefetchingUserEventDAO}
     * is created.
     *
     * @return The user-event DAO.
     */
    @Override
    public UserEventDAO getUserEventDAO() {
        return userEventDAOCache.get();
    }

    /**
     * Default item-event DAO implementation.  If the {@linkplain #getEventDAO() event DAO}
     * implements {@link ItemEventDAO}, it is returned directly; otherwise, a new {@link PrefetchingItemEventDAO}
     * is created.
     *
     * @return The item-event DAO.
     */
    @Override
    public ItemEventDAO getItemEventDAO() {
        return itemEventDAOCache.get();
    }

    /**
     * Default item DAO implementation.  If the {@linkplain #getEventDAO() event DAO}
     * implements {@link ItemDAO}, it is returned directly; otherwise, a new {@link PrefetchingItemDAO}
     * is created.
     *
     * @return The user-event DAO.
     */
    @Override
    public ItemDAO getItemDAO() {
        return itemDAOCache.get();
    }

    /**
     * Default user DAO implementation.  If the {@linkplain #getEventDAO() event DAO}
     * implements {@link UserDAO}, it is returned directly; otherwise, a new {@link PrefetchingUserDAO}
     * is created.
     *
     * @return The user-event DAO.
     */
    @Override
    public UserDAO getUserDAO() {
        return userDAOCache.get();
    }

    /**
     * Get the item name DAO.  The default implementation first checks if the event DAO implements
     * this interface, then the item DAO.
     *
     * @return The item name DAO, or {@code null} if none is defined.
     */
    public ItemNameDAO getItemNameDAO() {
        EventDAO dao = getEventDAO();
        if (dao instanceof ItemNameDAO) {
            return (ItemNameDAO) dao;
        }

        ItemDAO idao = getItemDAO();
        if (idao instanceof ItemNameDAO) {
            return (ItemNameDAO) idao;
        }

        return null;
    }

    @Override
    public void configure(LenskitConfiguration config) {
        logger.debug("generating configuration for {}", this);
        config.addComponent(getEventDAO());
        PreferenceDomain dom = getPreferenceDomain();
        if (dom != null) {
            logger.debug("using preference domain {}", dom);
            config.addComponent(dom);
        }
    }
}
