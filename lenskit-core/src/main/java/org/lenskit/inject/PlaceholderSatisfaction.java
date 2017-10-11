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
package org.lenskit.inject;

import org.grouplens.grapht.CachePolicy;
import org.grouplens.grapht.Instantiator;
import org.grouplens.grapht.LifecycleManager;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.grapht.reflect.SatisfactionVisitor;
import org.grouplens.grapht.util.ClassProxy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A placeholder satisfaction for graph rewriting.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class PlaceholderSatisfaction implements Satisfaction, Serializable {
    private static final long serialVersionUID = -1L;

    private final Class<?> removedType;

    public PlaceholderSatisfaction(Class<?> type) {
        removedType = type;
    }

    private Object writeReplace() {
        return new SerialProxy(removedType);
    }

    @Override
    public List<Desire> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Type getType() {
        return removedType;
    }

    @Override
    public Class<?> getErasedType() {
        return removedType;
    }

    @Override
    public boolean hasInstance() {
        return false;
    }

    @Override
    public <T> T visit(SatisfactionVisitor<T> visitor) {
        return visitor.visitNull();
    }

    @Override
    public CachePolicy getDefaultCachePolicy() {
        return CachePolicy.NEW_INSTANCE;
    }

    @Override
    public Instantiator makeInstantiator(@Nonnull Map<Desire, Instantiator> dependencies, @Nullable LifecycleManager lm) {
        throw new UnsupportedOperationException("placeholder node cannot create instantiators");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlaceholderSatisfaction that = (PlaceholderSatisfaction) o;

        if (removedType != null ? !removedType.equals(that.removedType) : that.removedType != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return removedType != null ? removedType.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "removed " + removedType.toString();
    }

    private static class SerialProxy implements Serializable {
        private static final long serialVersionUID = 1L;

        private final ClassProxy removedType;

        private SerialProxy(Class<?> type) {
            removedType = ClassProxy.of(type);
        }

        private Object readResolve() throws ObjectStreamException {
            try {
                return new PlaceholderSatisfaction(removedType.resolve());
            } catch (ClassNotFoundException e) {
                InvalidObjectException ex = new InvalidObjectException("class not found");
                ex.initCause(e);
                throw ex;
            }
        }
    }
}
