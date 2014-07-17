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
package org.grouplens.lenskit.inject;

import org.grouplens.grapht.CachePolicy;
import org.grouplens.grapht.Instantiator;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.grapht.reflect.SatisfactionVisitor;
import org.grouplens.grapht.util.ClassProxy;

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
    public Instantiator makeInstantiator(Map<Desire,Instantiator> dependencies) {
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
