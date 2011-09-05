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


import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;

/**
 * Basic preference implementation that stores data in fields.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class SimplePreference implements Preference, Cloneable {
    protected long userId;
    protected long itemId;
    protected double value;

    public SimplePreference(long uid, long iid, double v) {
        userId = uid;
        itemId = iid;
        value = v;
    }

    @Override
    public final long getUserId() {
        return userId;
    }
    @Override
    public final long getItemId() {
        return itemId;
    }
    @Override
    public final double getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;

        if (o instanceof Preference) {
            Preference p = (Preference) o;
            return userId == p.getUserId() && itemId == p.getItemId() && value == p.getValue();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Longs.hashCode(userId) ^ Longs.hashCode(itemId) ^ Doubles.hashCode(value);
    }

    @Override
    public Preference clone() {
        try {
            return (Preference) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Preference not cloneable", e);
        }
    }
}
