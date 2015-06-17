/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
 * JavaBeans specifying various LensKit objects.
 *
 * The classes in this package (and related packages) describe various LensKit objects; they are primarily used to
 * describe certain types of objects, such as data sources, without pulling in all of LensKit to do so; they are used
 * to allow one process (such as the Gradle plugin) to set up objects that will be used by another process (e.g. a
 * LensKit evaluator).
 *
 * @see org.lenskit.specs.data
 */
package org.lenskit.specs;
