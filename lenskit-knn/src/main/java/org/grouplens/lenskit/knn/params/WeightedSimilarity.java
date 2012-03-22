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
package org.grouplens.lenskit.knn.params;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import org.grouplens.lenskit.knn.SignificanceWeight;

/**
 * The inner similarity function to be used when using {@link SignificanceWeight}
 * to weight similarities.  Bind {@link SignificanceWeight} to either {@link ItemSimilarity}
 * or {@link UserSimilarity}, then bind the internal similarity function to this
 * parameter.
 *
 *  @see SignificanceWeight
 *  @see WeightThreshold
 */
// FIXME: keep this? if we do, it will not inherit from the default qualifier
// But we can also just say in(SignificanceWeight).bind(Similarity).to(asd)
@Documented
@Qualifier
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface WeightedSimilarity { }
