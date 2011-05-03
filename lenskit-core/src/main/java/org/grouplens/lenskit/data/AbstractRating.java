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
package org.grouplens.lenskit.data;


/**
 * Abstract rating implementation.  This just provides the {@link #equals(Object)}
 * and {@link #hashCode()} methods so classes don't have to duplicate the code.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public abstract class AbstractRating implements Rating {

    @Override
    public boolean equals(Object o) {
        if (o instanceof Rating) {
            Rating or = (Rating) o;
            return getUserId() == or.getUserId()
                && getItemId() == or.getItemId()
                && getRating() == or.getRating()
                && getTimestamp() == or.getTimestamp();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Long.valueOf(getUserId()).hashCode()
            ^ Long.valueOf(getItemId()).hashCode()
            ^ Double.valueOf(getRating()).hashCode()
            ^ Long.valueOf(getTimestamp()).hashCode();
    }
    
    public Rating clone() {
        try {
            return (Rating) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Rating not cloneable");
        }
    }

}