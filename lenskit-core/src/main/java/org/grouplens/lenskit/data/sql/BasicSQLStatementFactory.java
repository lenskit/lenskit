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
package org.grouplens.lenskit.data.sql;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.grouplens.lenskit.data.dao.SortOrder;
import org.grouplens.lenskit.data.dao.UnsupportedQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private String idColumn = "id";
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
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Set the name of the rating table.
     *
     * @param name The name of the rating table.
     */
    public void setTableName(@Nonnull String name) {
        tableName = name;
    }

    /**
     * Get the column of the event ID in the rating table.
     *
     * @return The name of the event ID column.
     */
    public String getIdColumn() {
        return idColumn;
    }

    /**
     * Set the name of the event ID column in the rating table.
     *
     * @param col The name of the event ID column.
     */
    public void setIdColumn(@Nonnull String col) {
        idColumn = col;
    }

    /**
     * Get the name of the user ID column in the rating table.
     *
     * @return the userColumn
     */
    public String getUserColumn() {
        return userColumn;
    }

    /**
     * Set the name of the user ID column in the rating table.
     *
     * @param col The name of the user column.
     */
    public void setUserColumn(@Nonnull String col) {
        userColumn = col;
    }

    /**
     * Get the name of the item ID column in the rating table.
     *
     * @return the itemColumn
     */
    public String getItemColumn() {
        return itemColumn;
    }

    /**
     * Set the name of the item ID column in the rating table.
     *
     * @param col The name of the item column.
     */
    public void setItemColumn(@Nonnull String col) {
        itemColumn = col;
    }

    /**
     * Get the name of the rating column in the rating table.
     *
     * @return the ratingColumn
     */
    public String getRatingColumn() {
        return ratingColumn;
    }

    /**
     * Set the name of the rating column in the rating table.
     *
     * @param col The name of the rating column.
     */
    public void setRatingColumn(@Nonnull String col) {
        ratingColumn = col;
    }

    /**
     * Get the name of the timestamp column in the rating table (or
     * {@code null} if there is no timestamp column).
     *
     * @return the timestampColumn
     */
    public String getTimestampColumn() {
        return timestampColumn;
    }

    /**
     * Set the name of the timestamp column in the rating table. Set to
     * {@code null} if there is no timestamp column.
     *
     * @param col The name of the timestamp column, or {@code null}.
     */
    public void setTimestampColumn(@Nullable String col) {
        timestampColumn = col;
    }

    @Override
    public String prepareUsers() {
        return String.format("SELECT DISTINCT %s FROM %s", userColumn, tableName);
    }

    @Override
    public String prepareUserCount() {
        return String.format("SELECT COUNT(DISTINCT %s) FROM %s", userColumn, tableName);
    }

    @Override
    public String prepareItems() {
        return String.format("SELECT DISTINCT %s FROM %s", itemColumn, tableName);
    }

    @Override
    public String prepareItemCount() {
        return String.format("SELECT COUNT(DISTINCT %s) FROM %s", itemColumn, tableName);
    }

    /**
     * Add the SELECT and FROM clauses to the query.
     *
     * @param query The query accumulator.
     */
    protected void rqAddSelectFrom(StringBuilder query) {
        query.append("SELECT ");
        query.append(idColumn);
        query.append(", ");
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
            throw new UnsupportedQueryException();
        }
    }

    /**
     * Finish a query (append a semicolon).
     *
     * @param query The query accumulator
     */
    protected void rqFinish(StringBuilder query) {
        query.append(";");
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
}
