/*
 * LensKit, a reference implementation of recommender algorithms.
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
/**
 * LensKit data access objects.
 * 
 * <p>LensKit uses <a href="http://java.sun.com/blueprints/corej2eepatterns/Patterns/DataAccessObject.html">Data Access Objects</a>
 * to obtain access to rating data.  These objects allow LensKit to query for
 * users, items, ratings, etc.  Some DAOs also support registering for notification
 * of changes.  The DAO should generally be a singleton, therefore, to support change
 * notification and registration throughout the system.
 * 
 * <p>DAOs use <emph>sessions</emph> to provide actual data access. Sessions
 * are not thread-safe and will often map to database connections.  Implementers
 * may also want to use thread-local variables and reference counting to cause
 * all sessions in the same thread to share a database connection, or explicitly
 * create sessions in servlet request filters (in which case they may want
 * {@link org.grouplens.lenskit.data.dao.UserItemDataSession#release()} to be
 * a no-op.
 * 
 * <p>The data access object makes no transactional or immutability guarantees,
 * and does not provide mutation.  An implementation is, of course, free to
 * provide mutation.  The recommender building process uses a {@link RatingBuildContext}
 * so that it can make multiple passes over a snapshot of the data.
 * 
 */
package org.grouplens.lenskit.data.dao;


