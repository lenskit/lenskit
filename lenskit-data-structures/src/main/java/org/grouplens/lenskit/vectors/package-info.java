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
/**
 * Sparse vectors and their operations. This package provides a sparse vector
 * framework for storing things such as rating vectors. Sparse vectors are
 * stored efficiently and have a key domain fixed at create time. Storage is
 * linear in the size of the key domain. See
 * {@link org.grouplens.lenskit.vectors.SparseVector} for more details.
 * 
 * <p>
 * To allow the type system to communicate valuable information about how
 * vectors are used while still preserving efficiency, sparse vectors come in
 * three forms: <em>read-only</em>, realized by
 * {@link org.grouplens.lenskit.vectors.SparseVector}, provides the basic sparse
 * vector interface; <em>read-write</em>, in
 * {@link org.grouplens.lenskit.vectors.MutableSparseVector}, provides an
 * interface where the values can be changed (e.g. for in-place addition or
 * subtraction); and <em>immutable</em>, in
 * {@link org.grouplens.lenskit.vectors.ImmutableSparseVector}, where the vector
 * is guaranteed to be unchanging and can be safely stored or shared across
 * threads without concern about the caller mutating it later.
 * 
 * <p>
 * There are further versions of immutable vectors with particular information
 * associated with them: {@link org.grouplens.lenskit.data.history.ItemVector},
 * {@link org.grouplens.lenskit.data.history.UserVector}, and their subclasses.
 * These classes are used for vectors of data associated with particular users.
 * 
 * <p>
 * The {@link org.grouplens.lenskit.vectors.SparseVector SparseVector} class
 * also provides utility methods for manipulating sparse vectors (e.g. the
 * {@link org.grouplens.lenskit.vectors.SparseVector#immutable()
 * SparseVector.immutable()} method for getting an immutable sparse vector).
 */
package org.grouplens.lenskit.vectors;