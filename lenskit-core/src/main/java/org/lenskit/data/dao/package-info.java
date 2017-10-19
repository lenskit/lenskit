/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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