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
 * Sparse vectors and their operations.  Sparse vectors map arbitrary long keys to values sparsely
 * and efficiently.  The keys can be negative.
 *
 * <p>Sparse vectors come in three flavors. Read-only vectors ({@link SparseVector});
 * this type is the base of the remaining types for each vector, and provides a read-only
 * interface to the vector.  Immutable vectors ({@link ImmutableSparseVector}) are immutable and cannot be
 * changed once created.  They can also be freely shared between threads.  Finally, mutable vectors
 * ({@link MutableSparseVector})
 * are mutable and not thread-safe.
 *
 * <p>This design allows read-only operations to be performed on any type of vector, while allowing
 * components to specifically work with and store vectors guaranteed to be immutable.
 *
 * <p>The {@link Vectors} class provides utility methods for working with vectors.
 */
package org.grouplens.lenskit.vectors;
