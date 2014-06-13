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
package org.grouplens.lenskit.data.sql;

/**
 * Key type for caching JDBC queries.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 2.1
 */
public class QueryKey {
    public static enum Type {
        USER, ITEM
    }

    private final Type queryType;
    private final long queryId;

    QueryKey(Type type, long id) {
        queryType = type;
        queryId = id;
    }

    public Type getQueryType() {
        return queryType;
    }

    public long getQueryId() {
        return queryId;
    }

    public static QueryKey user(long id) {
        return new QueryKey(Type.USER, id);
    }

    public static QueryKey item(long id) {
        return new QueryKey(Type.ITEM, id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueryKey queryKey = (QueryKey) o;

        if (queryId != queryKey.queryId) return false;
        if (queryType != queryKey.queryType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = queryType != null ? queryType.hashCode() : 0;
        result = 31 * result + (int) (queryId ^ (queryId >>> 32));
        return result;
    }
}
