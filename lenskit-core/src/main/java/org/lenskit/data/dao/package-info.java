/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
/**
 * LensKit data access objects.
 *
 * <p>LensKit uses <a href="http://java.sun.com/blueprints/corej2eepatterns/Patterns/DataAccessObject.html">Data Access Objects</a>
 * to obtain access to rating data.  These objects allow LensKit to query for
 * users, items, ratings, etc.  The master DAO interface is defined by {@link org.lenskit.data.dao.DataAccessObject}.
 * This interface can be reimplemented against other query APIs such as SQL databases, MongoDB, etc.  LensKit provides
 * basic implementations against static files and in-memory collections.
 *
 * <p>LensKit also uses intermediate layers, called <em>proxy DAOs</em>, such as {@link org.lenskit.data.ratings.RatingVectorPDAO}
 * that provide access to intermediate data structures that are usually computed from underlying data accessed via
 * the master DAO.
 *
 * <p>The data access objects make no transactional or immutability guarantees,
 * and do not provide mutation.  An implementation is, of course, free to
 * provide mutation.
 */
package org.lenskit.data.dao;