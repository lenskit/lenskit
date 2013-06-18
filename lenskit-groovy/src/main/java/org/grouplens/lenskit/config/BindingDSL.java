package org.grouplens.lenskit.config;

import groovy.lang.Closure;
import org.grouplens.grapht.Binding;
import org.grouplens.grapht.Module;
import org.grouplens.lenskit.core.AbstractConfigContext;
import org.grouplens.lenskit.core.LenskitConfigContext;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

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
        cl.setDelegate(this);
        cl.setResolveStrategy(Closure.DELEGATE_FIRST);
        cl.call();
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
        block.setDelegate(new BindingDSL(ctx));
        block.setResolveStrategy(Closure.DELEGATE_FIRST);
        block.call();
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
}
