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
package org.grouplens.lenskit.data.pref;

/**
 * A real-valued preference a user has for an item. Preferences can be
 * articulated by the user in the form of ratings, or they may be predicted or
 * otherwise computed. The distinction will generally be apparent from context.
 * 
 * <p>
 * Instances of this class should generally be immutable (or at least treated as
 * such). All exceptions must be clearly documented, and should only be in
 * contexts such as fast iteration.
 * 
 * <p>
 * Only user ID, item ID, and preference values are to be considered for
 * equality. Subclasses must <strong>not</strong> introduce additional fields
 * that need to be compared for equality.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public interface Preference {
    /**
     * Get the ID of the user whose preference this is.
     * 
     * @return The user ID.
     */
    long getUserId();

    /**
     * Get the ID of the item the preference is for.
     * 
     * @return The item ID.
     */
    long getItemId();

    /**
     * Get the preference value.
     * 
     * @return The preference value.
     */
    double getValue();

    /**
     * Clone the preference. The clone <b>must</b> be immutable (the
     * implementation of the clone can be mutable, in which case the immutable
     * guarantee is only required to be valid so long as the clone is never cast
     * to the mutable implementation).
     */
    Preference clone();
}
