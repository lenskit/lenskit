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
package org.grouplens.lenskit.params;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.grouplens.lenskit.data.history.HistorySummarizer;
import org.grouplens.lenskit.data.history.RatingVectorHistorySummarizer;
import org.grouplens.lenskit.params.meta.DefaultClass;
import org.grouplens.lenskit.params.meta.Parameter;

/**
 * The summarizer to use for summarizing user data prior to recommendation or
 * prediction.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@Documented
@Parameter(HistorySummarizer.class)
@DefaultClass(RatingVectorHistorySummarizer.class)
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface UserHistorySummary {
}