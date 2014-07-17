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
package org.grouplens.lenskit.data.sql;

import org.grouplens.lenskit.data.dao.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Default implementation of the SQL statement factory.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@SuppressWarnings("unused")
public class BasicSQLStatementFactory implements SQLStatementFactory {
    private static final Logger logger =
            LoggerFactory.getLogger(BasicSQLStatementFactory.class);
    @Nonnull
    private String tableName = "ratings";
    @Nonnull
    private String userColumn = "user";
    @Nonnull
    private String itemColumn = "item";
    @Nonnull
    private String ratingColumn = "rating";
    @Nullable
    private String timestampColumn = "timestamp";

    /**
     * Get the name of the rating table.
     *
     * @return The rating table name.
     */
    @Nonnull
    public String getTableName() {
        return tableName;
    }

    /**
     * Set the name of the rating table.
     *
     * @param name The name of the rating table.  This table name is used without escaping to build
     *             SQL queries, so include whatever escaping or quotations are needed to make the
     *             name valid in the backing DBMS in the name here.
     */
    public void setTableName(@Nonnull String name) {
        tableName = name;
    }

    /**
     * Get the name of the user ID column in the rating table.
     *
     * @return The user column name.
     */
    @Nonnull
    public String getUserColumn() {
        return userColumn;
    }

    /**
     * Set the name of the user ID column in the rating table.
     *
     * @param col The name of the user column.  This column name is used without escaping to build
     *            SQL queries, so include whatever escaping or quotations are needed to make the
     *            name valid in the backing DBMS in the name here.
     */
    public void setUserColumn(@Nonnull String col) {
        userColumn = col;
    }

    /**
     * Get the name of the item ID column in the rating table.
     *
     * @return The item column name.
     */
    @Nonnull
    public String getItemColumn() {
        return itemColumn;
    }

    /**
     * Set the name of the item ID column in the rating table.
     *
     * @param col The name of the item column.  This column name is used without escaping to build
     *            SQL queries, so include whatever escaping or quotations are needed to make the
     *            name valid in the backing DBMS in the name here.
     */
    public void setItemColumn(@Nonnull String col) {
        itemColumn = col;
    }

    /**
     * Get the name of the rating column in the rating table.
     *
     * @return The rating column name.
     */
    @Nonnull
    public String getRatingColumn() {
        return ratingColumn;
    }

    /**
     * Set the name of the rating column in the rating table.
     *
     * @param col The name of the rating column.  This column name is used without escaping to build
     *            SQL queries, so include whatever escaping or quotations are needed to make the
     *            name valid in the backing DBMS in the name here.
     */
    public void setRatingColumn(@Nonnull String col) {
        ratingColumn = col;
    }

    /**
     * Get the name of the timestamp column in the rating table (or {@code null} if there is no
     * timestamp column).
     *
     * @return The timestamp column name, or {@code null} if no timestamp is used.
     */
    @Nullable
    public String getTimestampColumn() {
        return timestampColumn;
    }

    /**
     * Set the name of the timestamp column in the rating table. Set to {@code null} if there is no
     * timestamp column.
     *
     * @param col The name of the timestamp column, or {@code null}.  This column name is used
     *            without escaping to build SQL queries, so include whatever escaping or quotations
     *            are needed to make the name valid in the backing DBMS in the name here.
     */
    public void setTimestampColumn(@Nullable String col) {
        timestampColumn = col;
    }

    @Override
    public String prepareUsers() {
        return String.format("SELECT DISTINCT %s FROM %s", userColumn, tableName);
    }

    @Override
    public String prepareItems() {
        return String.format("SELECT DISTINCT %s FROM %s", itemColumn, tableName);
    }

    /**
     * Add the SELECT and FROM clauses to the query.
     *
     * @param query The query accumulator.
     */
    protected void rqAddSelectFrom(StringBuilder query) {
        query.append("SELECT ");
        query.append(userColumn);
        query.append(", ");
        query.append(itemColumn);
        query.append(", ");
        query.append(ratingColumn);
        if (timestampColumn != null) {
            query.append(", ");
            query.append(timestampColumn);
        }
        query.append(" FROM ");
        query.append(tableName);
    }

    /**
     * Add an ORDER BY clause to a query.
     *
     * @param query The query accumulator
     * @param order The sort order.
     */
    protected void rqAddOrder(StringBuilder query, SortOrder order) {
        switch (order) {
        case ANY:
            break;
        case ITEM:
            query.append(" ORDER BY ");
            query.append(itemColumn);
            if (timestampColumn != null) {
                query.append(", ");
                query.append(timestampColumn);
            }
            break;
        case USER:
            query.append(" ORDER BY ").append(userColumn);
            if (timestampColumn != null) {
                query.append(", ");
                query.append(timestampColumn);
            }
            break;
        case TIMESTAMP:
            /* If we don't have timestamps, we return in any order. */
            if (timestampColumn != null) {
                query.append(" ORDER BY ").append(timestampColumn);
            }
            break;
        default:
            throw new IllegalArgumentException("unknown sort order " + order);
        }
    }

    /**
     * Finish a query (append a semicolon).
     *
     * @param query The query accumulator
     */
    protected void rqFinish(StringBuilder query) {
    }

    @Override
    public String prepareEvents(SortOrder order) {
        StringBuilder query = new StringBuilder();
        rqAddSelectFrom(query);
        rqAddOrder(query, order);
        rqFinish(query);
        logger.debug("Rating query: {}", query);
        return query.toString();
    }

    @Override
    public String prepareUserEvents() {
        StringBuilder query = new StringBuilder();
        rqAddSelectFrom(query);
        query.append(" WHERE ").append(userColumn).append(" = ?");
        rqFinish(query);
        logger.debug("User rating query: {}", query);
        return query.toString();
    }

    @Override
    public String prepareItemEvents() {
        StringBuilder query = new StringBuilder();
        rqAddSelectFrom(query);
        query.append(" WHERE ").append(itemColumn).append(" = ?");
        rqFinish(query);
        logger.debug("Item rating query: {}", query);
        return query.toString();
    }

    @Override
    public String prepareItemUsers() {
        StringBuilder query = new StringBuilder();
        query.append("SELECT DISTINCT ").append(userColumn)
             .append(" FROM ").append(tableName)
             .append(" WHERE ").append(itemColumn).append(" = ?");
        rqFinish(query);
        logger.debug("Item user query: {}", query);
        return query.toString();
    }
}
