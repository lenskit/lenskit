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
 * Baseline predictors.
 * 
 * <p>Baseline predictors are like rating predictors, but they provide an unboxed
 * {@link org.grouplens.lenskit.data.vector.SparseVector}-based interface and are
 * guaranteed to be able to predict for all users and items.  They are used for
 * things like normalizations and starting points for iterative methods.</p>
 */
package org.grouplens.lenskit.baseline;
