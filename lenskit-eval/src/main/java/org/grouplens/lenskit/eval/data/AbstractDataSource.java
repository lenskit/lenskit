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

import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.data.dao.*;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.traintest.CachingDAOProvider;

/**
 * Base class to help implement data sources.
 *
 * @since 2.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class AbstractDataSource implements DataSource {
    private transient volatile UserDAO userDAO;
    private transient volatile UserEventDAO userEventDAO;
    private transient volatile ItemDAO itemDAO;
    private transient volatile ItemEventDAO itemEventDAO;

    /**
     * Default user-event DAO implementation.  If the {@linkplain #getEventDAO() event DAO}
     * implements {@link UserEventDAO}, it is returned directly; otherwise, a new {@link org.grouplens.lenskit.data.dao.PrefetchingUserEventDAO}
     * is created.
     *
     * @return The user-event DAO.
     */
    @Override
    public UserEventDAO getUserEventDAO() {
        if (userEventDAO == null) {
            synchronized(this) {
                if (userEventDAO == null) {
                    EventDAO dao = getEventDAO();
                    if (dao instanceof UserEventDAO) {
                        userEventDAO = (UserEventDAO) dao;
                    } else {
                        userEventDAO = new PrefetchingUserEventDAO(dao);
                    }
                }
            }
        }
        return userEventDAO;
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
        if (itemEventDAO == null) {
            synchronized(this) {
                if (itemEventDAO == null) {
                    EventDAO dao = getEventDAO();
                    if (dao instanceof ItemEventDAO) {
                        itemEventDAO = (ItemEventDAO) dao;
                    } else {
                        itemEventDAO = new PrefetchingItemEventDAO(dao);
                    }
                }
            }
        }
        return itemEventDAO;
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
        if (itemDAO == null) {
            synchronized(this) {
                if (itemDAO == null) {
                    EventDAO dao = getEventDAO();
                    if (dao instanceof ItemDAO) {
                        itemDAO = (ItemDAO) dao;
                    } else {
                        itemDAO = new PrefetchingItemDAO(dao);
                    }
                }
            }
        }
        return itemDAO;
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
        if (userDAO == null) {
            synchronized(this) {
                if (userDAO == null) {
                    EventDAO dao = getEventDAO();
                    if (dao instanceof UserDAO) {
                        userDAO = (UserDAO) dao;
                    } else {
                        userDAO = new PrefetchingUserDAO(dao);
                    }
                }
            }
        }
        return userDAO;
    }

    @Override
    public LenskitConfiguration getConfiguration() {
        LenskitConfiguration config = new LenskitConfiguration();
        config.addComponent(getEventDAO());
        PreferenceDomain dom = getPreferenceDomain();
        if (dom != null) {
            config.addComponent(dom);
        }
        config.bind(PrefetchingUserDAO.class)
              .toProvider(CachingDAOProvider.User.class);
        config.bind(PrefetchingUserEventDAO.class)
              .toProvider(CachingDAOProvider.UserEvent.class);
        config.bind(PrefetchingItemDAO.class)
              .toProvider(CachingDAOProvider.Item.class);
        config.bind(PrefetchingItemEventDAO.class)
              .toProvider(CachingDAOProvider.ItemEvent.class);
        return config;
    }
}
