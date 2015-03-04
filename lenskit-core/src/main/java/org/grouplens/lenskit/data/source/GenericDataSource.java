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

import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.specs.SpecificationContext;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Generic data source backed by a single DAO object, implementing at least {@link EventDAO}.  If
 * the object implements other DAO interfaces, it is used to provide those; otherwise, they are
 * supplied by the prefetching DAOs.
 *
 * @since 2.2
 */
public class GenericDataSource extends AbstractDataSource {
    private String name;
    private EventDAO dao;
    private PreferenceDomain domain;

    public GenericDataSource(String name, EventDAO dao) {
        this(name, dao, null);
    }

    public GenericDataSource(String name, EventDAO dao, PreferenceDomain dom) {
        this.name = name;
        this.dao = dao;
        domain = dom;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PreferenceDomain getPreferenceDomain() {
        return domain;
    }

    @Override
    public EventDAO getEventDAO() {
        return dao;
    }

    @Override
    public long lastModified() {
        return 0;
    }

    @Nonnull
    @Override
    public Map<String, Object> toSpecification(SpecificationContext context) {
        throw new UnsupportedOperationException("generic data sources cannot be specified");
    }
}
