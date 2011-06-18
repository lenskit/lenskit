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
package org.grouplens.lenskit.pico;

import java.lang.reflect.Type;

import org.grouplens.lenskit.Builder;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.adapters.AbstractAdapter;
import org.picocontainer.injectors.AbstractInjector.CyclicDependencyException;

/**
 * BuilderAdapter is a ComponentAdapter that can use {@link Builder} classes or
 * instances to build instances of specific types. If a Builder class is
 * provided (e.g. <code>Class&lt? extends Builder&lt? extends T&gt&gt</code>), a
 * new instance of the builder is created using injection with PicoContainer. If
 * a Builder instance is provided, the Builder is used directly
 * 
 * @author Michael Ludwig
 * @param <T>
 */
public class BuilderAdapter<T> extends AbstractAdapter<T> {
    private static final long serialVersionUID = 1L;

    private final Class<? extends Builder<? extends T>> builderType;

    private transient Builder<? extends T> builder;
    private transient ThreadLocal<Boolean> cycleGuard;
    
    public BuilderAdapter(Object key, Class<? extends Builder<? extends T>> builderType) {
        super(key, getBuiltType(builderType));
        this.builderType = builderType;
        builder = null;
    }
    
    @SuppressWarnings("unchecked")
    public BuilderAdapter(Object key, Builder<? extends T> builder) {
        this(key, (Class<? extends Builder<? extends T>>) builder.getClass());
        this.builder = builder;
    }
    
    public Class<? extends Builder<? extends T>> getBuilderType() {
        return builderType;
    }
    
    @SuppressWarnings("unchecked")
    private static <T> Class<? extends T> getBuiltType(Class<? extends Builder<? extends T>> builderType) {
        try {
            return (Class<? extends T>) builderType.getMethod("build").getReturnType();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
        if (cycleGuard == null)
            cycleGuard = new ThreadLocal<Boolean>();
        
        if (Boolean.TRUE.equals(cycleGuard.get()))
            throw new CyclicDependencyException(getComponentImplementation());

        cycleGuard.set(Boolean.TRUE);
        try {
            if (builder == null)
                builder = container.getComponent(builderType);
            return (builder == null ? null : builder.build());
        } finally {
            cycleGuard.set(Boolean.FALSE);
        }
    }

    @Override
    public void verify(PicoContainer container) throws PicoCompositionException { }

    @Override
    public String getDescriptor() {
        return "RecommenderComponnetBuilderAdapter<" + getComponentImplementation()+">";
    }
}
