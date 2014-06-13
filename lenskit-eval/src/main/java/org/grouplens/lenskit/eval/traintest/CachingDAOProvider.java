/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval.traintest;

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import org.grouplens.lenskit.data.dao.*;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

/**
 * Caching provider to reduce the duplication of DAO instances in evaluations.
 *
 * <p>This assumes that each unique DAO object is unchanging.  That may or may not be a safe
 * assumption outside the evaluator!</p>
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class CachingDAOProvider<T> implements Provider<T> {
    private static final ConcurrentMap<Class<?>,Cache<EventDAO,Object>> CACHE_MAP =
            Maps.newConcurrentMap();

    private static void registerProviderClass(Class<?> cls) {
        Cache<EventDAO,Object> cache = CACHE_MAP.get(cls);
        if (cache == null) {
            cache = CacheBuilder.newBuilder()
                                .weakKeys()
                                .softValues()
                                .build();
            CACHE_MAP.putIfAbsent(cls, cache);
        }
    }

    private final EventDAO eventDAO;

    CachingDAOProvider(EventDAO dao) {
        registerProviderClass(getClass());
        eventDAO = dao;
    }

    protected abstract T create(EventDAO dao);

    @SuppressWarnings("unchecked")
    @Override
    public T get() {
        Cache<EventDAO,Object> cache = CACHE_MAP.get(getClass());
        assert cache != null;
        try {
            return (T) cache.get(eventDAO, new Callable<Object>() {
                @Override
                public Object call() {
                    return create(eventDAO);
                }
            });
        } catch (ExecutionException e) {
            throw Throwables.propagate(e.getCause());
        }
    }

    public static final class User extends CachingDAOProvider<PrefetchingUserDAO> {
        @Inject
        public User(EventDAO dao) {
            super(dao);
        }

        @Override
        protected PrefetchingUserDAO create(EventDAO dao) {
            return new PrefetchingUserDAO(dao);
        }
    }

    public static final class Item extends CachingDAOProvider<PrefetchingItemDAO> {
        @Inject
        public Item(EventDAO dao) {
            super(dao);
        }

        @Override
        protected PrefetchingItemDAO create(EventDAO dao) {
            return new PrefetchingItemDAO(dao);
        }
    }

    public static final class UserEvent extends CachingDAOProvider<PrefetchingUserEventDAO> {
        @Inject
        public UserEvent(EventDAO dao) {
            super(dao);
        }

        @Override
        protected PrefetchingUserEventDAO create(EventDAO dao) {
            return new PrefetchingUserEventDAO(dao);
        }
    }

    public static final class ItemEvent extends CachingDAOProvider<PrefetchingItemEventDAO> {
        @Inject
        public ItemEvent(EventDAO dao) {
            super(dao);
        }

        @Override
        protected PrefetchingItemEventDAO create(EventDAO dao) {
            return new PrefetchingItemEventDAO(dao);
        }
    }
}
