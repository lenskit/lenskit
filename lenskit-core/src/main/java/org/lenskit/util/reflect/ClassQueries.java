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
