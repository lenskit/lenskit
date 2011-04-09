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
package org.grouplens.lenskit.params.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an annotation as defining a recommender parameter.
 *
 * <p>LensKit makes extensive use of annotations to define recommender parameters.
 * This annotation is a meta-annotation that should be applied to any annotation
 * defining a recommender parameter.  It serves to document the annotation as
 * a parameter annotation, and is also used by {@link org.grouplens.lenskit.config.RecommenderCoreModule}
 * to automatically set up default parameter values.  It may also be used by an
 * annotation processor someday to automatically handle parameter plumbing.
 * <p>
 * Parameters can additionally be annotated by the various default annotations
 * in this package to specify their default values.
 *
 * @see org.grouplens.lenskit.config.RecommenderCoreModule
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@Documented
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Parameter {

}
