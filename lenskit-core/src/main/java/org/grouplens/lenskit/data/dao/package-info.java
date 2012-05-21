/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
 * users, items, ratings, etc.  DAOs correspond generally to database connections
 * or their equivalent, so they should only be used in a single thread.  When
 * threads need to be able to create DAOs, {@link org.grouplens.lenskit.data.dao.DAOFactory} objects
 * should be used.
 *
 * <p>The data access object makes no transactional or immutability guarantees,
 * and does not provide mutation.  An implementation is, of course, free to
 * provide mutation.  The recommender building process uses a
 * {@link org.grouplens.lenskit.data.snapshot.PreferenceSnapshot PreferenceSnapshot}
 * so that it can make multiple passes over a snapshot of the data.
 *
 */
package org.grouplens.lenskit.data.dao;


