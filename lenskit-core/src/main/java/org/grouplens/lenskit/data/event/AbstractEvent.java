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
package org.grouplens.lenskit.data.event;

import com.google.common.primitives.Longs;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public abstract class AbstractEvent implements Event {
    @Override
    public boolean equals(Object o) {
        if (o instanceof Event) {
            Event oe = (Event) o;
            return getId() == oe.getId();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Longs.hashCode(getId());
    }
    
    /**
     * Default clone implementation; hands off to {@link Object#clone()}.
     * @return The cloned event.
     */
    @Override
    public Event clone() {
        try {
            return (Event) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Rating not cloneable");
        }
    }
}