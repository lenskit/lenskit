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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.grouplens.lenskit.params.meta.DefaultBoolean;
import org.grouplens.lenskit.params.meta.DefaultClass;
import org.grouplens.lenskit.params.meta.DefaultDouble;
import org.grouplens.lenskit.params.meta.DefaultInt;
import org.picocontainer.BindKey;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.injectors.AbstractInjectionFactory;
import org.picocontainer.injectors.AbstractInjector;

public class ParameterAnnotationInjector<T> extends AbstractInjector<T> {
    private static final long serialVersionUID = 1L;

    public static class Factory extends AbstractInjectionFactory {
        private static final long serialVersionUID = 1L;

        @SuppressWarnings("unchecked")
        @Override
        public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor componentMonitor,
                                                              LifecycleStrategy lifecycleStrategy,
                                                              Properties componentProperties, Object componentKey,
                                                              Class<T> componentImplementation, Parameter... parameters) throws PicoCompositionException {
            return wrapLifeCycle(new ParameterAnnotationInjector<T>(componentKey, componentImplementation, 
                                                                    parameters, componentMonitor), 
                                 lifecycleStrategy);
        }
        
    }
    
    private transient ThreadLocal<Boolean> cycleGuard;
    
    private transient Constructor<T> constructor;
    private transient Object[] constructorParameterKeys;
    private transient boolean constructorFound;
    
    private transient Method[] methods;
    private transient Object[] methodKeys;
    
    public ParameterAnnotationInjector(Object componentKey, Class<?> componentImplementation,
                                       Parameter[] parameters, ComponentMonitor monitor) {
        super(componentKey, componentImplementation, parameters, monitor, false);
    }

    @SuppressWarnings("unchecked")
    protected Constructor<T> getConstructor() {
        Constructor<?>[] constructors = getComponentImplementation().getConstructors();
        if (constructors.length > 0) {
            Constructor<?> maxArgConstructor = constructors[0];
            int maxNumArgs = maxArgConstructor.getParameterTypes().length;
            for (int i = 1; i < constructors.length; i++) {
                int numArgs = constructors[i].getParameterTypes().length;
                if (numArgs > maxNumArgs) {
                    maxNumArgs = numArgs;
                    maxArgConstructor = constructors[i];
                }
            }
            
            // use the constructor with the largest number of arguments
            return (Constructor<T>) maxArgConstructor;
        } else {
            // no constructor to use, so return null
            return null;
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Object[] getConstructorParameterKeys(Constructor<T> constructor) {
        Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        
        Object[] keys = new Object[parameterTypes.length];
        for (int i = 0; i < keys.length; i++) {
            Type type = parameterTypes[i];//box(parameterTypes[i]);
            Annotation binding = null;
            for (Annotation annot: parameterAnnotations[i]) {
                if (annot.annotationType().getAnnotation(org.grouplens.lenskit.params.meta.Parameter.class) != null) {
                    binding = annot;
                    break;
                }
            }
            
            if (binding == null)
                keys[i] = type;
            else
                keys[i] = new BindKey((Class<?>) type, binding.annotationType());
        }
        
        return keys;
    }
    
    protected Annotation getMethodBinding(Method method) {
        Annotation[] methodAnnotations = method.getAnnotations();
        for (Annotation annot: methodAnnotations) {
            if (annot.annotationType().getAnnotation(org.grouplens.lenskit.params.meta.Parameter.class) != null)
                return annot;
        }

        // This is only called on methods that have 1 argument
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (Annotation annot: parameterAnnotations[0]) {
            if (annot.annotationType().getAnnotation(org.grouplens.lenskit.params.meta.Parameter.class) != null)
                return annot;
        }
        
        return null;
    }
    
    protected boolean isInjectedMethod(Method m) {
        final String prefix = "set";
        String name = m.getName();
        boolean nameMatch = name.startsWith(prefix) && 
                            name.length() > prefix.length() && 
                            Character.isUpperCase(name.charAt(prefix.length()));
                            // FIXME: is nameMatch sufficient?
        return nameMatch;
    }
    
    protected Method[] getInjectedMethods() {
        Method[] allMethods = getComponentImplementation().getMethods();
        List<Method> filteredMethods = new ArrayList<Method>();
        for (Method m: allMethods) {
            if (m.getReturnType().equals(void.class) &&
                m.getParameterTypes().length == 1 &&
                isInjectedMethod(m)) {
                // The method has a void return type, takes 1 argument
                // and is "injectable" so we want to remember it
                filteredMethods.add(m);
            }
        }
        
        return filteredMethods.toArray(new Method[filteredMethods.size()]);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Object[] getInjectedMethodKeys(Method[] methods) {
        Object[] keys = new Object[methods.length];
        for (int i = 0; i < methods.length; i++) {
            Type type = methods[i].getParameterTypes()[0];//box(methods[i].getParameterTypes()[0]);
            Annotation binding = getMethodBinding(methods[i]);
            
            keys[i] = (binding == null ? type : new BindKey((Class<?>) type, binding.annotationType()));
        }
        
        return keys;
    }
    
    private T getInstance(PicoContainer container, Constructor<T> constructor, Object[] constructorKeys) {
        long start = System.currentTimeMillis();
        
        // Build required objects for the constructor
        Object[] constructorArgs = new Object[constructorKeys.length];
        for (int i = 0; i < constructorArgs.length; i++) {
            Object arg = getInstance(container, constructorKeys[i]);
            if (arg == null) {
                // FIXME: add nullable support here
                throw new PicoCompositionException("Unable to resolve dependency for " + constructorKeys[i]);
            }
            constructorArgs[i] = arg;
        }
        
        // instantiate instance
        try {
            T instance = constructor.newInstance(constructorArgs);
            long end = System.currentTimeMillis();
            
            // If we've gotten here, we know this is the constructor being used
            // so notify the monitor (although we do both steps at once since
            // we don't know when 'instantiation' started
            currentMonitor().instantiating(container, this, constructor);
            currentMonitor().instantiated(container, this, constructor, instance, constructorArgs, end - start);
            
            return instance;
        } catch (Exception e) {
            throw new PicoCompositionException("Error instantiating component", e);
        }
    }
    
    private Object getInstance(PicoContainer container, Object key) {
        RuntimeException failure = null;
        Object result = null;
        try {
            // First try to resolve everything with 
            result = container.getComponent(key);
        } catch(RuntimeException re) {
            failure = re;
        }
        
        if (result == null && key instanceof BindKey) {
            // Look to see if we can look up a default type based on the parameter annotation
            BindKey<?> bindKey = (BindKey<?>) key;
            DefaultClass dfltClass = bindKey.getAnnotation().getAnnotation(DefaultClass.class);
            DefaultInt dfltInt = bindKey.getAnnotation().getAnnotation(DefaultInt.class);
            DefaultDouble dfltDouble = bindKey.getAnnotation().getAnnotation(DefaultDouble.class);
            DefaultBoolean dfltBoolean = bindKey.getAnnotation().getAnnotation(DefaultBoolean.class);
            
            // We can't modify the container, though, because it may not be mutable and if it was
            // we could get ConcurrentModificationExceptions when getComponents() is called on it
            if (dfltClass != null) {
                try {
                    result = container.getComponent(dfltClass.value());
                } catch(RuntimeException re) {
                    if (failure == null)
                        failure = re;
                }
            } else if (dfltInt != null)
                result = dfltInt.value();
            else if (dfltDouble != null)
                result = dfltDouble.value();
            else if (dfltBoolean != null)
                result = dfltBoolean.value();

            // If the BindKey's annotation didn't give us a default type, see if can 
            // use the BindKey's type
            if (result == null) {
                try {
                    result = container.getComponent(bindKey.getType());
                } catch(RuntimeException re) {
                    if (failure == null)
                        failure = re;
                }
            }
        }
        
        if (result != null)
            return result;
        else if (failure != null)
            throw failure;
        else
            return null;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public T getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
        if (cycleGuard == null)
            cycleGuard = new ThreadLocal<Boolean>();
        if (Boolean.TRUE.equals(cycleGuard.get()))
            throw new CyclicDependencyException(getComponentImplementation());
        
        cycleGuard.set(Boolean.TRUE);
        try {
            T instance = null;
            if (!constructorFound) {
                // haven't found the proper constructor
                Constructor<?>[] allConstructors = getComponentImplementation().getConstructors();
                Arrays.sort(allConstructors, new Comparator<Constructor<?>>() {
                    @Override
                    public int compare(Constructor<?> o1, Constructor<?> o2) {
                        return o2.getParameterTypes().length - o1.getParameterTypes().length;
                    }
                });
                
                // check every constructor to see if we can create an instance
                // since it's sorted, we start with the greediest
                Constructor<T> c = null;
                PicoCompositionException lastPCE = null;
                for (int i = 0; i < allConstructors.length; i++) {
                    try {
                        c = (Constructor<T>) allConstructors[i];
                        Object[] keys = getConstructorParameterKeys(c);
                        
                        // check for cycles now and skip any constructor that depends on this type
                        boolean simpleCycleFound = false;
                        for (Object key: keys) {
                            if (key instanceof Class && ((Class<?>) key).isAssignableFrom(getComponentImplementation())
                                || key == getComponentKey()) {
                                simpleCycleFound = true;
                                break;
                            }
                        }
                        if (simpleCycleFound)
                            continue;
                        
                        instance = getInstance(container, c, keys);
                        if (instance != null) {
                            // found the constructor
                            constructor = c;
                            constructorParameterKeys = keys;
                            lastPCE = null; // clear any exception if we've found a valid constructor
                            break;
                        }
                    } catch(PicoCompositionException pce) {
                        lastPCE = pce;
                    }
                }
                
                // set this to true (since we have one, or we've tried and failed and don't want to search again)
                constructorFound = true;
                if (lastPCE != null) {
                    currentMonitor().instantiationFailed(container, this, c, lastPCE);
                    throw new PicoCompositionException("Could not find satisfiable constructor for " + getComponentImplementation(), lastPCE);
                }
            } else if (constructor != null) {
                // have searched and found a constructor previously
                try {
                    instance = getInstance(container, constructor, constructorParameterKeys);
                } catch(PicoCompositionException pce) {
                    currentMonitor().instantiationFailed(container, this, constructor, pce);
                    throw pce; // continue throwing
                }
            }
            
            if (instance == null) {
                PicoCompositionException failure = new PicoCompositionException("Unable to instantiate an instance of " + getComponentImplementation());
                currentMonitor().instantiationFailed(container, this, constructor, failure);
                throw failure;
            }
            
            // we have an instance, proceed with method injection
            if (methods == null) {
                methods = getInjectedMethods();
                methodKeys = getInjectedMethodKeys(methods);
            }
            
            // invoke all extra setter injectors
            for (int i = 0; i < methods.length; i++) {
                long start = System.currentTimeMillis();
                Object arg = getInstance(container, methodKeys[i]);
                if (arg == null) {
                    // FIXME: add nullable support here
                    throw new PicoCompositionException("Unable to resolve dependency for " + methodKeys[i]);
                } else {
                    try {
                        currentMonitor().invoking(container, this, methods[i], instance, new Object[] { arg });
                        methods[i].invoke(instance, arg);
                        currentMonitor().invoked(container, this, methods[i], instance, System.currentTimeMillis() - start, new Object[] { arg }, null);
                    } catch (Exception e) {
                        currentMonitor().invocationFailed(methods[i], instance, e);
                        throw new PicoCompositionException("Error calling setter injection method " + methods[i], e);
                    }
                }
            }
            
            return instance;
        } finally {
            cycleGuard.set(Boolean.FALSE);
        }
    }
}
