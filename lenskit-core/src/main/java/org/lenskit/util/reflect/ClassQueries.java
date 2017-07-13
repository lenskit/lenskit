/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.util.reflect;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.lenskit.inject.Shareable;
import org.lenskit.util.parallel.MaybeThreadSafe;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;

/**
 * Various utility function queries on classes.
 */
public final class ClassQueries {
    /**
     * Query whether a class is marked as thread-safe.  A class can be marked as thread-safe through applying one of
     * the following annotations:
     *
     * - {@link org.lenskit.inject.Shareable}
     * - {@link net.jcip.annotations.Immutable}
     * - {@link net.jcip.annotations.ThreadSafe}
     *
     * Note that the `javax.concurrent` annotations are **not** supported, as they do not have `RUNTIME` retention.
     *
     * @param cls The class.
     * @return `true` if the class is marked as thread-safe.
     */
    public static boolean isThreadSafe(@Nonnull Class<?> cls) {
        Annotation a = cls.getAnnotation(Shareable.class);
        if (a == null) {
            a = cls.getAnnotation(Immutable.class);
        }
        if (a == null) {
            a = cls.getAnnotation(ThreadSafe.class);
        }
        return a != null;
    }

    /**
     * Query whether an object is thread-safe.  This first checks for the {@link org.lenskit.util.parallel.MaybeThreadSafe}
     * interface; if that is not present, then it checks the class using {@link #isThreadSafe(Class)}.
     *
     * @param obj The object.
     * @return `true` if the object is marked as thread-safe.  Null objects are trivially considered to be thread-safe.
     */
    public static boolean isThreadSafe(Object obj) {
        if (obj == null) {
            return true;
        } else if (obj instanceof MaybeThreadSafe) {
            return ((MaybeThreadSafe) obj).isThreadSafe();
        } else {
            return isThreadSafe(obj.getClass());
        }
    }
}
