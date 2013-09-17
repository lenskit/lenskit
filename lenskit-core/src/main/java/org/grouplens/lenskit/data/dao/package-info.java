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
/**
 * LensKit data access objects.
 *
 * <p>LensKit uses <a href="http://java.sun.com/blueprints/corej2eepatterns/Patterns/DataAccessObject.html">Data Access Objects</a>
 * to obtain access to rating data.  These objects allow LensKit to query for
 * users, items, ratings, etc.
 *
 * <p>Many DAO operations are expected to be fast, usually with appropriate caching.  It is typical
 * for DAOs to be instantiated once per {@link org.grouplens.lenskit.Recommender} (and therefore
 * once per request in a web environment), and to cache aggressively in instance variables.  More
 * sophisticated implementations using shared caches or services such as Memcache are certainly
 * feasible.
 *
 * <p>The streaming DAO implementations build up a cache once per instance.  They aren't really
 * suitable for anything besides recommender evaluation, typically.
 *
 * <p>The data access objects make no transactional or immutability guarantees,
 * and does not provide mutation.  An implementation is, of course, free to
 * provide mutation.
 */
package org.grouplens.lenskit.data.dao;