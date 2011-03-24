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
package org.grouplens.lenskit.params;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.grouplens.lenskit.params.meta.DefaultDouble;
import org.grouplens.lenskit.params.meta.Parameter;

import com.google.inject.BindingAnnotation;

/**
 * Parameter to damp means as recommended by Simon Funk.
 *
 * <p>The mean damping factor is used to bias a mean towards the global mean.
 * For a collection of `n` items `x_i`, a damping factor `D`, and a global mean
 * `µ`, the damped mean is computed as `(\sum_{x_i} x_i + Dµ)/(n + D)`.
 * See <a href="http://sifter.org/~simon/journal/20061211.html">Netflix Update:
 * Try This at Home</a> by Simon Funk for documentation of this enhancement.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@BindingAnnotation
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Parameter
@DefaultDouble(0)
public @interface MeanDamping {
}
