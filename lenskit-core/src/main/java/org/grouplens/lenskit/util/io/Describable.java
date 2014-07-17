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
package org.grouplens.lenskit.util.io;

/**
 * Objects supporting persistent descriptions of their identity.  Components implementing this
 * interface can write information about their identity to a description, suitable for hashing, etc.
 * Unlike serialization, this is not reversible; objects are expected to write enough information to
 * uniquely identify themselves in a persistent fashion, but not necessarily to reconstruct
 * themselves.
 * <p>
 * Describable objects will generally be immutable. If they are not, they should write their current
 * description.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 2.1
 */
public interface Describable {
    /**
     * Write this class's description to a sink. Anything relevant to this object's identity should
     * be digested; the idea is that, barring hash collisions, two objects with the same digest are
     * equivalent.  Used for things like deterministically generating cache file names.
     *
     * @param writer The description writer to use.
     */
    void describeTo(DescriptionWriter writer);
}
