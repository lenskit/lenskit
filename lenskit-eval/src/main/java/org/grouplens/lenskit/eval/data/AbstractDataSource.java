/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval.data;

import org.grouplens.lenskit.data.dao.*;

/**
 * Base class to help implement data sources.
 *
 * @since 2.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class AbstractDataSource implements DataSource {
    /**
     * Get an event DAO from the provider.
     * @return The event DAO.
     */
    @Override
    public EventDAO getEventDAO() {
        return getEventDAOProvider().get();
    }

    /**
     * Default user-event DAO implementation.  If the {@linkplain #getEventDAO() event DAO}
     * implements {@link UserEventDAO}, it is returned directly; otherwise, a new {@link org.grouplens.lenskit.data.dao.PrefetchingUserEventDAO}
     * is created.
     *
     * @return The user-event DAO.
     */
    @Override
    public UserEventDAO getUserEventDAO() {
        EventDAO dao = getEventDAO();
        if (dao instanceof UserEventDAO) {
            return (UserEventDAO) dao;
        } else {
            return new PrefetchingUserEventDAO(dao);
        }
    }

    /**
     * Default item-event DAO implementation.  If the {@linkplain #getEventDAO() event DAO}
     * implements {@link ItemEventDAO}, it is returned directly; otherwise, a new {@link org.grouplens.lenskit.data.dao.PrefetchingItemEventDAO}
     * is created.
     *
     * @return The item-event DAO.
     */
    @Override
    public ItemEventDAO getItemEventDAO() {
        EventDAO dao = getEventDAO();
        if (dao instanceof ItemEventDAO) {
            return (ItemEventDAO) dao;
        } else {
            return new PrefetchingItemEventDAO(dao);
        }
    }

    /**
     * Default item DAO implementation.  If the {@linkplain #getEventDAO() event DAO}
     * implements {@link ItemDAO}, it is returned directly; otherwise, a new {@link org.grouplens.lenskit.data.dao.PrefetchingItemDAO}
     * is created.
     *
     * @return The user-event DAO.
     */
    @Override
    public ItemDAO getItemDAO() {
        EventDAO dao = getEventDAO();
        if (dao instanceof ItemDAO) {
            return (ItemDAO) dao;
        } else {
            return new PrefetchingItemDAO(dao);
        }
    }

    /**
     * Default user DAO implementation.  If the {@linkplain #getEventDAO() event DAO}
     * implements {@link UserDAO}, it is returned directly; otherwise, a new {@link org.grouplens.lenskit.data.dao.PrefetchingUserDAO}
     * is created.
     *
     * @return The user-event DAO.
     */
    @Override
    public UserDAO getUserDAO() {
        EventDAO dao = getEventDAO();
        if (dao instanceof UserDAO) {
            return (UserDAO) dao;
        } else {
            return new PrefetchingUserDAO(dao);
        }
    }
}
