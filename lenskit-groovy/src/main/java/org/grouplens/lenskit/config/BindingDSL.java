/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.config;

import groovy.lang.Closure;
import org.grouplens.grapht.Binding;
import org.grouplens.grapht.Module;
import org.grouplens.lenskit.core.AbstractConfigContext;
import org.grouplens.lenskit.core.LenskitConfigContext;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.data.pref.PreferenceDomainBuilder;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Groovy DSL definition for configuring LensKit recommenders. This class is the base class of
 * configuration scripts and the delegate against which configuration blocks are run.
 *
 * <p>The fact that this extends {@link AbstractConfigContext} is basically an implementation
 * detail, to make sure that we always provide proxies for all the methods.
 *
 * @since 1.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@SuppressWarnings("unused")
public class BindingDSL extends AbstractConfigContext {
    private LenskitConfigContext context;

    /**
     * Construct a new delegate.
     *
     * @param ctx The context to configure.
     */
    BindingDSL(LenskitConfigContext ctx) {
        context = ctx;
    }

    /**
     * Get the LensKit context.
     * @return The LensKit context.
     */
    public LenskitConfigContext getContext() {
        return context;
    }

    /**
     * Use a closure as additional configuration
     *
     * @param cl A closure that is run on this context to do additional configuration.
     */
    public void include(Closure<?> cl) {
        ConfigHelpers.callWithDelegate(cl, this);
    }

    /**
     * Include a module in this configuration.
     *
     * @param mod The module to include.
     */
    public void include(Module mod) {
        mod.configure(context);
    }

    /** @see LenskitConfigContext#bind(Class) */
    @Override
    public <T> Binding<T> bind(Class<T> type) {
        return context.bind(type);
    }

    /** @see LenskitConfigContext#bind(Class, Class) */
    @Override
    public <T> Binding<T> bind(Class<? extends Annotation> qual, Class<T> type) {
        return context.bind(qual, type);
    }

    /** @see LenskitConfigContext#bindAny(Class) */
    @Override
    public <T> Binding<T> bindAny(Class<T> type) {
        return context.bindAny(type);
    }

    /** @see LenskitConfigContext#set(Class) */
    @Override
    @SuppressWarnings("rawtypes")
    public Binding set(Class<? extends Annotation> param) {
        return context.set(param);
    }

    private LenskitConfigContext configure(LenskitConfigContext ctx, Closure<?> block) {
        ConfigHelpers.callWithDelegate(block, new BindingDSL(ctx));
        return ctx;
    }

    /** @see LenskitConfigContext#within(Class) */
    @Override
    public LenskitConfigContext within(Class<?> type) {
        return context.within(type);
    }

    /**
     * Enclose a block of configuration in a context.  The block is invoked with a delegate that
     * adds bindings within the specified context.
     *
     * @param type  The type to match for the context.
     * @param block The configuration block.
     * @return The configuration context.
     * @see LenskitConfigContext#within(Class)
     */
    public LenskitConfigContext within(Class<?> type, Closure<?> block) {
        return configure(within(type), block);
    }

    /** @see LenskitConfigContext#within(Class, Class) */
    @Override
    public LenskitConfigContext within(@Nullable Class<? extends Annotation> qualifier, Class<?> type) {
        return context.within(qualifier, type);
    }

    /**
     * Enclose a block of configuration in a context.
     *
     * @param qualifier The qualifier.
     * @param type  The type to match for the context.
     * @param block The configuration block.
     * @return The configuration context.
     * @see LenskitConfigContext#within(Class, Class)
     * @see #within(Class, Closure)
     */
    public LenskitConfigContext within(@Nullable Class<? extends Annotation> qualifier,
                                       Class<?> type, Closure<?> block) {
        return configure(within(qualifier, type), block);
    }

    /** @see LenskitConfigContext#within(Annotation, Class) */
    @Override
    public LenskitConfigContext within(@Nullable Annotation qualifier, Class<?> type) {
        return context.within(qualifier, type);
    }

    /**
     * Enclose a block of configuration in a context.
     *
     * @param qualifier The qualifier.
     * @param type  The type to match for the context.
     * @param block The configuration block.
     * @return The configuration context.
     * @see LenskitConfigContext#within(Annotation, Class)
     * @see #within(Class, Closure)
     */
    public LenskitConfigContext within(@Nullable Annotation qualifier,
                                       Class<?> type, Closure<?> block) {
        return configure(within(qualifier, type), block);
    }

    /** @see LenskitConfigContext#at(Class) */
    @Override
    public LenskitConfigContext at(Class<?> type) {
        return context.at(type);
    }

    /**
     * Configure inside an anchored context using a block.
     * @param type The type.
     * @param block The configuration block.
     * @return The context.
     * @see #within(Class, Closure)
     */
    public LenskitConfigContext at(Class<?> type, Closure<?> block) {
        return configure(at(type), block);
    }

    /** @see LenskitConfigContext#at(Class, Class) */
    @Override
    public LenskitConfigContext at(@Nullable Class<? extends Annotation> qualifier, Class<?> type) {
        return context.at(qualifier, type);
    }

    /**
     * Enclose a block of configuration in a context.
     *
     * @param qualifier The qualifier.
     * @param type  The type to match for the context.
     * @param block The configuration block.
     * @return The configuration context.
     * @see LenskitConfigContext#at(Class, Class)
     * @see #at(Class, Closure)
     */
    public LenskitConfigContext at(@Nullable Class<? extends Annotation> qualifier,
                                       Class<?> type, Closure<?> block) {
        return configure(at(qualifier, type), block);
    }

    /** @see LenskitConfigContext#at(Annotation, Class) */
    @Override
    public LenskitConfigContext at(@Nullable Annotation qualifier, Class<?> type) {
        return context.at(qualifier, type);
    }

    /**
     * Enclose a block of configuration in a context.
     *
     * @param qualifier The qualifier.
     * @param type  The type to match for the context.
     * @param block The configuration block.
     * @return The configuration context.
     * @see LenskitConfigContext#at(Annotation, Class)
     * @see #at(Class, Closure)
     */
    public LenskitConfigContext at(@Nullable Annotation qualifier,
                                       Class<?> type, Closure<?> block) {
        return configure(at(qualifier, type), block);
    }

    /**
     * Make and bind a preference domain.  With this method, this:
     * <pre>
     *     domain minimum: 1, maximum: 5
     * </pre>
     * <p>is equivalent to:
     * <pre>
     *     bind PreferenceDomain to prefDomain(minimum: 1, maximum: 5)
     * </pre>
     *
     * @param args The arguments.
     * @return The preference domain.
     * @see #prefDomain(java.util.Map)
     */
    public PreferenceDomain domain(Map<String,Object> args) {
        PreferenceDomain dom = prefDomain(args);
        bind(PreferenceDomain.class).to(dom);
        return dom;
    }

    /**
     * Make a preference domain.  This method takes three named arguments:
     * <dl>
     *     <dt>minimum</dt>
     *     <dd>The minimum rating</dd>
     *     <dt>maximum</dt>
     *     <dd>The maximum rating</dd>
     *     <dt>precision (optional)</dt>
     *     <dd>The preference precision.</dd>
     * </dl>
     *
     * @param args The arguments.
     * @return The preference domain.
     * @see org.grouplens.lenskit.data.pref.PreferenceDomain
     */
    public PreferenceDomain prefDomain(Map<String,Object> args) {
        return ConfigHelpers.buildObject(new PreferenceDomainBuilder(), args);
    }
}
