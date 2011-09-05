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
 * A mutable preference object for use in fast iteration and other mutable
 * contexts.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class MutablePreference extends SimplePreference {

    /**
     * Construct a new preference with default values. The user and item IDs are
     * both set to 0, and the value to {@link Double#NaN}.
     */
    public MutablePreference() {
        super(0,0,Double.NaN);
    }

    /**
     * Create a new mutable preference.
     * @param uid The user ID.
     * @param iid The item ID.
     * @param v The preference value.
     */
    public MutablePreference(long uid, long iid, double v) {
        super(uid, iid, v);
    }

    public final void setUserId(long uid) {
        userId = uid;
    }

    public final void setItemId(long iid) {
        itemId = iid;
    }

    public final void setValue(double v) {
        value = v;
    }

    @Override
    public Preference clone() {
        return new SimplePreference(userId, itemId, value);
    }

}
