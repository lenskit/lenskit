/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.grouplens.lenskit.data.dao.SortOrder;
import org.grouplens.lenskit.data.dao.UnsupportedQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class BasicSQLStatementFactory implements SQLStatementFactory {
    private static final Logger logger =
            LoggerFactory.getLogger(BasicSQLStatementFactory.class);
    private String tableName = "ratings";
    private String idColumn = "id";
    private String userColumn = "user";
    private String itemColumn = "item";
    private String ratingColumn = "rating";
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
     * @param tableName the tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Get the column of the event ID in the rating table.
     * @return The name of the event ID column.
     */
    public String getIdColumn() {
        return idColumn;
    }

    /**
     * Set the name of the event ID column in the rating table.
     * @param col The name of the event ID column.
     */
    public void setIdColumn(String col) {
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
     * @param userColumn the userColumn to set
     */
    public void setUserColumn(String userColumn) {
        this.userColumn = userColumn;
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
     * @param itemColumn the itemColumn to set
     */
    public void setItemColumn(String itemColumn) {
        this.itemColumn = itemColumn;
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
     * @param ratingColumn the ratingColumn to set
     */
    public void setRatingColumn(String ratingColumn) {
        this.ratingColumn = ratingColumn;
    }

    /**
     * Get the name of the timestamp column in the rating table (or
     * <tt>null</tt> if there is no timestamp column).
     *
     * @return the timestampColumn
     */
    public String getTimestampColumn() {
        return timestampColumn;
    }

    /**
     * Set the name of the timestamp column in the rating table. Set to
     * <tt>null</tt> if there is no timestamp column.
     *
     * @param timestampColumn the timestampColumn to set
     */
    public void setTimestampColumn(String timestampColumn) {
        this.timestampColumn = timestampColumn;
    }

    @Override
    public PreparedStatement prepareUsers(Connection dbc) throws SQLException {
        String query =
            String.format("SELECT DISTINCT %s FROM %s", userColumn, tableName);
        return dbc.prepareStatement(query);
    }

    @Override
    public PreparedStatement prepareUserCount(Connection dbc)
            throws SQLException {
        String query =
            String.format("SELECT COUNT(DISTINCT %s) FROM %s", userColumn,
                          tableName);
        return dbc.prepareStatement(query);
    }

    @Override
    public PreparedStatement prepareItems(Connection dbc) throws SQLException {
        String query =
            String.format("SELECT DISTINCT %s FROM %s", itemColumn, tableName);
        return dbc.prepareStatement(query);
    }

    @Override
    public PreparedStatement prepareItemCount(Connection dbc)
            throws SQLException {
        String query =
            String.format("SELECT COUNT(DISTINCT %s) FROM %s", itemColumn,
                          tableName);
        return dbc.prepareStatement(query);
    }

    /**
     * Add the SELECT and FROM clauses to the query
     *
     * @param query
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
     * Add an ORDER BY clause to a query
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
            query.append(" ORDER BY " + userColumn);
            if (timestampColumn != null) {
                query.append(", ");
                query.append(timestampColumn);
            }
            break;
        case TIMESTAMP:
            /* If we don't have timestamps, we return in any order. */
            if (timestampColumn != null) {
                query.append(" ORDER BY " + timestampColumn);
            }
            break;
        default:
            throw new UnsupportedQueryException();
        }
    }

    protected void rqFinish(StringBuilder query) {
        query.append(";");
    }

    @Override
    public PreparedStatement prepareEvents(Connection dbc, SortOrder order)
            throws SQLException {
        StringBuilder query = new StringBuilder();
        rqAddSelectFrom(query);
        rqAddOrder(query, order);
        rqFinish(query);
        logger.debug("Rating query: {}", query);
        return dbc.prepareStatement(query.toString());
    }

    @Override
    public PreparedStatement prepareUserEvents(Connection dbc)
            throws SQLException {
        StringBuilder query = new StringBuilder();
        rqAddSelectFrom(query);
        query.append(" WHERE " + userColumn + " = ?");
        rqFinish(query);
        logger.debug("User rating query: {}", query);
        return dbc.prepareStatement(query.toString());
    }

    @Override
    public PreparedStatement prepareItemEvents(Connection dbc)
            throws SQLException {
        StringBuilder query = new StringBuilder();
        rqAddSelectFrom(query);
        query.append(" WHERE " + itemColumn + " = ?");
        rqFinish(query);
        logger.debug("Item rating query: {}", query);
        return dbc.prepareStatement(query.toString());
    }
}
