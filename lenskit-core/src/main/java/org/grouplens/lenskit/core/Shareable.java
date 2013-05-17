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
package org.grouplens.lenskit.core;

import java.lang.annotation.*;

/**
 * Mark a component implementation as shareable. Shareable components can be shared
 * between recommender sessions. Things like item-item models should be shareable.
 * <p>
 * Shareable components must meet the following requirements:
 * <ul>
 * <li>Be thread-safe</li>
 * <li>Be serializable (or externalizable)</li>
 * </ul>
 * <p>
 * Shareable components will be reused as much as possible. If a shareable component
 * has no non-transient non-shareable dependencies, then it will be created once per
 * recommender <i>engine</i> rather than per-recommender.
 * <p>
 * The Shareable annotation should be on the component implementation, not interface.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Shareable {
}
