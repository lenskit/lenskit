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

import org.grouplens.lenskit.params.meta.Parameter;

import com.google.inject.BindingAnnotation;

/**
 * The number of threads to use for some operation.  The default value for this
 * parameter in the default modules is the number of CPUs available (determined
 * via {@link Runtime#availableProcessors()}).
 *
 * <p>Due to limitations in Java's annotation mechanism, the default value for
 * this class is special-cased in the base recommender module.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@BindingAnnotation
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Parameter
public @interface ThreadCount {
}
