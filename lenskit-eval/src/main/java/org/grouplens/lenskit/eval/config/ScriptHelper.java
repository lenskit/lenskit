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
package org.grouplens.lenskit.eval.config;

import com.google.common.base.*;
import com.google.common.collect.Iterables;
import groovy.lang.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.Builder;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.grouplens.lenskit.config.GroovyUtils;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Utilities for searching for methods of configurable objects.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@SuppressWarnings("rawtypes")
public class ScriptHelper {
    private EvalScriptEngine engine;

    public ScriptHelper(EvalScriptEngine engine) {
        this.engine = engine;
    }

    Iterable<Method> getOneArgMethods(Object obj, final String name) {
        return Iterables.filter(Arrays.asList(obj.getClass().getMethods()), new Predicate<Method>() {
            @Override
            public boolean apply(@Nullable Method method) {
                if (method == null) {
                    return false;
                } else {
                    return method.getName().equals(name) && method.getParameterTypes().length == 1;
                }
            }
        });
    }

    /**
     * Search for a method with a specified BuiltBy, or a single-argument method with a parameter that
     * can be built. Used when we have a closure to build a directive argument.
     *
     * @param self    The command to search.
     * @param args    The arguments.
     * @return A closure to prepare and invoke the method, or {@code null} if no such method can be
     *         found.
     * @see org.grouplens.lenskit.eval.config.EvalScriptEngine#getBuilderForType(Class)
     */
    private Supplier<Object> findBuildableMethod(final Object self, String name, final Object[] args) {
        Supplier<Object> result = null;
        for (final Method method: getOneArgMethods(self, name)) {
            Class<?> param = method.getParameterTypes()[0];
            final Class<? extends Builder> bldClass;
            BuiltBy annot = method.getAnnotation(BuiltBy.class);
            if (annot == null) {
                annot = param.getAnnotation(BuiltBy.class);
            }
            if (annot != null) {
                bldClass = annot.value();
            } else {
                bldClass = engine.getBuilderForType(param);
            }

            if (bldClass != null) {
                if (result != null) {
                    throw new RuntimeException("multiple buildable methods named " + name);
                }
                result = new Supplier<Object>() {
                    @Override
                    public Object get() {
                        Builder builder;
                        try {
                            builder = constructAndConfigure(bldClass, args);
                            return method.invoke(self, builder.build());
                        } catch (ReflectiveOperationException e) {
                            throw Throwables.propagate(e);
                        }
                    }
                };
            }
        }

        return result;
    }

    /**
     * Find a method that should be invoked multiple times, if the argument is iterable.  The
     * argument may be iterated multiple times.
     *
     * @param self The configurable object.
     * @param name The method name.
     * @param args The arguments.
     * @return A thunk that will invoke the method.
     */
    private Supplier<Object> findMultiMethod(final Object self, String name, final Object[] args) {
        if (args.length != 1) return null;
        // the argument is a list
        final Object arg = args[0];
        if (!(arg instanceof Iterable)) {
            return null;
        }

        final Iterable<?> objects = (Iterable<?>) arg;

        Supplier<Object> result = null;
        for (final Method method: getOneArgMethods(self, name)) {
            Class ptype = method.getParameterTypes()[0];
            boolean good = Iterables.all(objects, Predicates.or(Predicates.isNull(),
                                                                Predicates.instanceOf(ptype)));
            if (good) {
                if (result != null) {
                    throw new RuntimeException("multiple compatible methods named " + name);
                } else {
                    result = new Supplier<Object>() {
                        @Override
                        public Object get() {
                            for (Object obj: objects) {
                                try {
                                    method.invoke(self, obj);
                                } catch (IllegalAccessException e) {
                                    throw Throwables.propagate(e);
                                } catch (InvocationTargetException e) {
                                    if (e.getCause() != null) {
                                        throw Throwables.propagate(e);
                                    }
                                }
                            }
                            return null;
                        }
                    };
                }
            }
        }

        return result;
    }

    /**
     * Look for a method on an object.
     *
     * @param self The object.
     * @param name The method name.
     * @param args The method arguments.
     * @return A thunk invoking the method, or {@code null} if no such method is found.
     */
    private Supplier<Object> findMethod(final Object self, String name, Object[] args) {
        Object[] objects = Arrays.copyOf(args, args.length);
        Class<?>[] types = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            if (objects[i] != null) {
                types[i] = objects[i].getClass();
            }
        }

        MetaClass metaclass = InvokerHelper.getMetaClass(self);

        MetaMethod mm = metaclass.pickMethod(name, types);

        // try some simple transformations
        // transform a trailing closure to a function
        if (mm == null && objects.length > 0) {
            Object lastArg = objects[objects.length - 1];
            if (lastArg instanceof Closure) {
                Class<?>[] at2 = Arrays.copyOf(types, types.length);
                at2[objects.length - 1] = Function.class;
                mm = metaclass.pickMethod(name, at2);
                if (mm != null) {
                    objects[objects.length - 1] =  new ClosureFunction((Closure) lastArg);
                }
            }
        }


        // try instantiating a single class
        if (mm == null && objects.length == 1 && objects[0] instanceof Class) {
            final Class<?> cls = (Class) objects[0];
            Class[] at2 = {cls};
            final MetaMethod method = metaclass.pickMethod(name, at2);
            if (method != null) {
                return new Supplier<Object>() {
                    @Override
                    public Object get() {
                        Object[] objs;
                        try {
                            objs = new Object[]{cls.newInstance()};
                        } catch (ReflectiveOperationException e) {
                            throw Throwables.propagate(e);
                        }
                        return method.doMethodInvoke(self, objs);
                    }
                };
            }
        }

        if (mm == null) {
            return null;
        } else {
            final MetaMethod method = mm;
            final Object[] finalArgs = objects;
            return new Supplier<Object>() {
                @Override
                public Object get() {
                    return method.doMethodInvoke(self, finalArgs);
                }
            };
        }
    }

    private Object makeConfigDelegate(final Object target) {
        ConfigDelegate annot = target.getClass().getAnnotation(ConfigDelegate.class);
        if (annot == null) {
            return new DefaultConfigDelegate(this, target);
        } else {
            Class<?> dlgClass = annot.value();
            try {
                return ConstructorUtils.invokeConstructor(dlgClass, target);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("error constructing " + dlgClass, e);
            }
        }
    }

    /**
     * Split an array of arguments into arguments a trailing closure.
     *
     * @param args The argument array.
     * @return A pair consisting of the arguments, except for any trailing closure, and the closure. If
     *         {@var args} does not have end with a closure, {@code Pair.of(args, null)} is returned.
     */
    public Pair<Object[], Closure> splitClosure(Object[] args) {
        if (args.length > 0 && args[args.length - 1] instanceof Closure) {
            return Pair.of(Arrays.copyOf(args, args.length - 1),
                           (Closure) args[args.length - 1]);
        } else {
            return Pair.of(args, null);
        }
    }

    /**
     * Construct and configure a configurable object.  This instantiates the class, using the provided
     * arguments.  If the last argument is a closure, it is witheld and used to configure the object
     * after it is constructed.  No extra type coercion is performed.
     *
     * <p>If the object has an {@code evalConfig} property, that property is set to the engine's
     * configuration.
     *
     * @param type The type to construct.
     * @param args The arguments.
     * @return The constructed and configured object.
     */
    private <T> T constructAndConfigure(Class<T> type, Object[] args) throws NoSuchMethodException {
        Pair<Object[], Closure> split = splitClosure(args);
        MetaClass metaclass = InvokerHelper.getMetaClass(type);

        Object obj;
        try {
            obj = metaclass.invokeConstructor(split.getLeft());
        } catch (GroovyRuntimeException e) {
            Throwables.propagateIfInstanceOf(e.getCause(), NoSuchMethodException.class);
            throw e;
        }

        MetaProperty configProp = InvokerHelper.getMetaClass(obj).getMetaProperty("evalConfig");
        if (configProp != null) {
            configProp.setProperty(obj, engine.getConfig());
        }

        if (split.getRight() != null) {
            GroovyUtils.callWithDelegate(split.getRight(), makeConfigDelegate(obj));
        }

        return type.cast(obj);
    }

    /**
     * Find an external method (a builder or task) and return a closure that, when invoked,
     * constructs and configures it.  It does <strong>not</strong> invoke the builder or task, that
     * is left up to the caller.
     *
     * @param name   The method name.
     * @return The constructed and configured object corresponding to this method.
     */
    public Object callExternalMethod(String name, Object... args) throws NoSuchMethodException {
        final Class<?> mtype = engine.lookupMethod(name);
        if (mtype != null) {
            try {
                return constructAndConfigure(mtype, args);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("error instantiating and configuring " + mtype.toString(), e);
            }
        } else {
            throw new NoSuchMethodException(name);
        }

    }

    public Object invokeConfigurationMethod(final Object target, final String name, final Object[] args) {
        Preconditions.checkNotNull(target, "target object");

        final String setterName = "set" + StringUtils.capitalize(name);
        final String adderName = "add" + StringUtils.capitalize(name);
        Supplier<Object> inv;
        // directly invoke
        inv = findMethod(target, name, args);
        if (inv == null) {
            inv = findBuildableMethod(target, name, args);
        }
        // invoke a setter
        if (inv == null) {
            inv = findMethod(target, setterName, args);
        }
        // invoke a buildable setter
        if (inv == null) {
            inv = findBuildableMethod(target, setterName, args);
        }
        // invoke an adder
        if (inv == null) {
            inv = findMethod(target,  adderName, args);
        }
        // add from a list
        if (inv == null) {
            inv = findMultiMethod(target, adderName, args);
        }
        // invoke a buildable adder
        if (inv == null) {
            inv = findBuildableMethod(target, adderName, args);
        }

        if (inv != null) {
            return inv.get();
        } else {
            // try to invoke the method directly
            return DefaultGroovyMethods.invokeMethod(target, name, args);
        }

    }

    private static class ClosureFunction implements Function {
        public ClosureFunction(Closure cl) {
            closure = cl;
        }

        @Override
        public Object apply(Object input) {
            return closure.call(input);
        }

        private Closure closure;
    }
}
