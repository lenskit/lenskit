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
package org.grouplens.lenskit.data.event;

import javax.annotation.Nullable;

import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.pref.Preference;

/**
 * A rating is an expression of preference for an item by a user.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public interface Rating extends Event {
    /**
     * Get the expressed preference. If this is an "unrate" event, the
     * preference will be {@code null}.
     *
     * @return The expressed preference.
     */
    @Nullable
    Preference getPreference();

    @Override
    Rating copy();
}
