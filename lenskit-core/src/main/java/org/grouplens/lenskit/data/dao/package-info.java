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
 * <p>DAOs use <emph>sessions</emph> to provide actual data access.  The session
 * is managed internally by the DAO and is not directly exposed, except for the
 * {@link UserItemDataAccessObject#openSession()} and
 * {@link UserItemDataAccessObject#closeSession()} methods. These methods open
 * and close a session on the current thread; all other DAO access except for
 * registering listeners must happen within a context.  Therefore, client code
 * must open a session before it can use the recommender, and close that session
 * when it is done.
 * 
 * <p>The data access object makes no transactional or immutability guarantees,
 * and does not provide mutation.  An implementation is, of course, free to
 * provide mutation.  The recommender building process uses a {@link RatingBuildContext}
 * so that it can make multiple passes over a snapshot of the data.
 * 
 */
package org.grouplens.lenskit.data.dao;


