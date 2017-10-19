/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.util.describe;

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
