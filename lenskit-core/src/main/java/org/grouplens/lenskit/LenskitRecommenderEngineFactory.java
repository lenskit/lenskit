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
package org.grouplens.lenskit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nullable;

import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.snapshot.PackedRatingSnapshot;
import org.grouplens.lenskit.data.snapshot.RatingSnapshot;
import org.grouplens.lenskit.params.meta.Built;
import org.grouplens.lenskit.params.meta.DefaultBuilder;
import org.grouplens.lenskit.params.meta.Parameters;
import org.grouplens.lenskit.pico.BuilderAdapter;
import org.grouplens.lenskit.pico.DependencyMonitor;
import org.grouplens.lenskit.pico.JustInTimePicoContainer;
import org.grouplens.lenskit.pico.ParameterAnnotationInjector;
import org.grouplens.lenskit.util.PrimitiveUtils;
import org.picocontainer.BindKey;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.InjectionFactory;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.AbstractInjectionFactory;
import org.picocontainer.lifecycle.StartableLifecycleStrategy;

/**
 * {@link RecommenderEngineFactory} that builds a LensKit recommender engine.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class LenskitRecommenderEngineFactory implements RecommenderEngineFactory {
    private final Map<Class<? extends Annotation>, Object> annotationBindings;
    private final Map<Class<?>, Object> defaultBindings;
    private DAOFactory<? extends RatingDataAccessObject> daoManager;
    
    /**
     * Create a new engine factory with no DAO factory.
     * 
     * <p>
     * Unless a DAO manager is provided by
     * {@link #setDAOFactory(DAOFactory)}, the factory and all
     * resulting engines cannot open DAOs themselves, so the {@link #create()}
     * and {@link RecommenderEngine#open()} methods will not work. In that case,
     * the {@link #create(RatingDataAccessObject)} and
     * {@link RecommenderEngine#open(RatingDataAccessObject, boolean)} methods
     * must be used instead.
     */
    public LenskitRecommenderEngineFactory() {
        this(null);
    }
    
    /**
     * Construct a new engine factory that will get DAOs from the specified
     * DAO factory.
     * @param daom The DAO factory for obtaining data access.
     */
    public LenskitRecommenderEngineFactory(@Nullable DAOFactory<? extends RatingDataAccessObject> daom) {
        annotationBindings = new HashMap<Class<? extends Annotation>, Object>();
        defaultBindings = new HashMap<Class<?>, Object>();
        daoManager = daom;
        
        setComponent(RatingSnapshot.class, PackedRatingSnapshot.class);
        
        // Technically this isn't needed since the default type is configured,
        // but it's nice to show explicit bindings for these snapshots
        // Disabled 2011-05-25 by MDE to avoid pulling in normalizers unnecessarily
        // bind(NormalizedSnapshot.class, NormalizedRatingSnapshot.class);
    }

    /**
     * Get the DAO manager configured for this factory.
     * @return The DAO manager, or <tt>null</tt> if no DAO manager is configured.
     */
    public @Nullable DAOFactory<? extends RatingDataAccessObject> getDAOFactory() {
        return daoManager;
    }
    
    /**
     * Set the DAO manager.
     * @param daom
     */
    public void setDAOFactory(@Nullable DAOFactory<? extends RatingDataAccessObject> daom) {
        daoManager = daom;
    }
    
    @SuppressWarnings("unchecked")
    public synchronized void set(Class<? extends Annotation> param, Number instance) {
        Class<?> paramType = PrimitiveUtils.box(Parameters.getParameterType(param));
        if (Number.class.isAssignableFrom(paramType) && instance instanceof Number)
            instance = PrimitiveUtils.cast((Class<? extends Number>) paramType, (Number) instance);
        updateBindings(annotationBindings, param, instance);
    }
    
    /**
     * Set the instnace to be used for a particular component.
     * 
     * <p><b>Note:</b> LensKit does not currently support multiple component
     * types with the same annotation.</p>
     * @param <T>
     * @param annot The annotation for the component.
     * @param type The component's type.
     * @param instance The component instance.
     */
    public synchronized <T> void setComponent(Class<? extends Annotation> annot, Class<T> type, T instance) {
        // Proceed with normal instance binding
        validateAnnotation(annot);
        
        if (instance != null) {
            // TODO Review to deal with types specified on parameter annotations
        }
        // TODO Actually use the class type in bindings
        updateBindings(annotationBindings, annot, instance);
    }
    
    /**
     * Set the implementation to be used for a particular component.
     * 
     * <p><b>Note:</b> LensKit does not currently support multiple component
     * types with the same annotation.</p>
     * 
     * @param param The component annotation.
     * @param instanceType The type to use for this component.
     */
    public synchronized <T> void setComponent(Class<? extends Annotation> param, Class<T> type, Class<? extends T> instanceType) {
        // FIXME: Actually use the type
        validateAnnotation(param);
        // Verify that the types match
        Class<?> paramType = PrimitiveUtils.box(Parameters.getParameterType(param));
        if (instanceType != null && !paramType.isAssignableFrom(type))
            throw new IllegalArgumentException(instanceType + " is incompatible with the type expected by parameter " + param.getClass() 
                                               + ", expected " + paramType);
        
        if (instanceType.getAnnotation(Built.class) != null) {
            setBuilder(param, type, findBuilder(instanceType));
        } else {
            // This class can be created on its own
            updateBindings(annotationBindings, param, instanceType);
        }
    }
    
    public synchronized <T> void setBuilder(Class<? extends Annotation> param, Class<T> type, Class<? extends Builder<? extends T>> builderType) {
        validateAnnotation(param);
        if (builderType != null) {
            // Verify that the builder generates the appropriate type
            Class<?> paramType = PrimitiveUtils.box(Parameters.getParameterType(param));
            Class<?> builtType = getBuiltType(builderType);
            if (!paramType.isAssignableFrom(builtType))
                throw new IllegalArgumentException(builderType + " creates instances incompatible with the type expected by parameter " + param.getClass()
                                                   + ", expected " + paramType + ", but was " + builtType);
        }
        updateBindings(annotationBindings, param, builderType);
    }
    
    public synchronized <M> void setComponent(Class<M> type, Class<? extends M> instanceType) {
        if (type == null)
            throw new NullPointerException("Super-type cannot be null");
        // Verify that the instanceType is actually a subtype
        if (instanceType != null && !type.isAssignableFrom(instanceType))
            throw new IllegalArgumentException(instanceType + " is not a subclass of " + type);
        
        if (instanceType != null && instanceType.getAnnotation(Built.class) != null) {
            // Bind a builder instead
            setBuilder(type, findBuilder(instanceType));
        } else {
            // Type can be created on its own
            updateBindings(defaultBindings, type, instanceType);
        }
    }
    
    public synchronized <M> void setComponent(Class<M> type, M instance) {
        if (type == null)
            throw new NullPointerException("Super-type cannot be null");
        // Verify instance is actually a subtype
        if (instance != null && !type.isInstance(instance))
            throw new IllegalArgumentException(instance + " is not a subclass of " + type);
        
        // Since we have an instance, there is no distinction between if it
        // uses a builder or not (it's already been built)
        updateBindings(defaultBindings, type, instance);
    }
    
    public synchronized <M> void setBuilder(Class<M> superType, Class<? extends Builder<? extends M>> builderType) {
        if (superType == null)
            throw new NullPointerException("Super-type cannot be null");
        if (builderType != null) {
            // Verify that the builder generates a proper subtype
            Class<?> builtType = getBuiltType(builderType);
            if (!superType.isAssignableFrom(builtType))
                throw new IllegalArgumentException(builderType + " creates instances of " + builtType 
                                                   + ", which are not subclasses of " + superType);
        }
        updateBindings(defaultBindings, superType, builderType);
    }
    
    private void validateAnnotation(Class<? extends Annotation> param) {
        if (param == null)
            throw new NullPointerException("Annotation cannot be null");
        
        if (!Parameters.isParameter(param))
            throw new IllegalArgumentException("Annotation must be annotated with Parameter");
    }
    
    private <K, V> void updateBindings(Map<K, ? super V> bindings, K key, V value) {
        synchronized(this) {
            if (value == null)
                bindings.remove(key);
            else
                bindings.put(key, value);
        }
    }
    
    @Override
    public RecommenderEngine create() {
        if (daoManager == null)
            throw new IllegalStateException("create() called with no DAO factory");
        RatingDataAccessObject dao = daoManager.create();
        try {
            return create(dao, null, true);
        } finally {
            dao.close();
        }
    }
    
    /**
     * Create a new recommender engine from a particular DAO. The factory's DAO
     * manager, if set, is still used by the resulting engine to open sessions.
     * 
     * @review If the user provides a DAO and has set a DAO Factory, do we use
     *         or ignore the DAO Factory?
     * @param dao The DAO to user for building the recommender.
     * @return A new recommender engine. The engine does <b>not</b> depend on
     *         the DAO, but will use DAOs obtained when recommenders are opened.
     */
    public RecommenderEngine create(RatingDataAccessObject dao) {
        return create(dao, null, false);
    }
    
    protected RecommenderEngine create(RatingDataAccessObject dao, PicoContainer parent, boolean useManager) {
        Map<Class<? extends Annotation>, Object> annotationBindings;
        Map<Class<?>, Object> defaultBindings;
        
        synchronized(this) {
            // Clone configuration so that this build is thread safe
            annotationBindings = new HashMap<Class<? extends Annotation>, Object>(this.annotationBindings);
            defaultBindings = new HashMap<Class<?>, Object>(this.defaultBindings);
        }
        
        DependencyMonitor daoMonitor = new DependencyMonitor(RatingDataAccessObject.class);
        BuilderTrackingAdapterFactory jitBuilderFactory = new BuilderTrackingAdapterFactory(new ParameterAnnotationInjector.Factory());
        MutablePicoContainer buildContainer = new JustInTimePicoContainer(new Caching().wrap(jitBuilderFactory), 
                                                                          new StartableLifecycleStrategy(daoMonitor),
                                                                          parent, daoMonitor);
        
        // We assume that these generated bindings include configurations for a build context
        // and recommender type
        Map<Object, Object> keyBindings = generateBindings(annotationBindings, defaultBindings);

        // Push all bindings into the build container
        for (Entry<Object, Object> binding: keyBindings.entrySet()) {
            if (binding.getValue() instanceof ComponentAdapter)
                buildContainer.addAdapter((ComponentAdapter<?>) binding.getValue());
            else
                buildContainer.addComponent(binding.getKey(), binding.getValue());
        }
        
        // Stash a dao into the container for the build
        buildContainer.addComponent(dao);
        // Construct all known objects to discover dependencies and to build things made by builders
        buildContainer.getComponents();

        // Must make sure to close all RatingSnapshots
        for (RatingSnapshot snapshot: buildContainer.getComponents(RatingSnapshot.class))
            snapshot.close();

        Set<Object> daoDependentKeys = daoMonitor.getDependentKeys();

        // Create a new container that will be used by the RecommenderEngine.
        // This container will not contain builders or any of the components depending on a dao
        //  - built instances will be stored instead
        //  - dao dependencies are placed in a separate child container created per-session
        MutablePicoContainer recommenderContainer = new JustInTimePicoContainer(new ParameterAnnotationInjector.Factory(), parent);
        Map<Object, Object> sessionBindings = new HashMap<Object, Object>();
        
        // Configure recommender container with all bindings that don't depend on a dao
        // FIXME: we really ought to configure the JIT bound objects too in-case parameters/defaults
        //   change what gets bound
        for (Entry<Object, Object> binding: keyBindings.entrySet()) {
            if (!isBindingValidAfterBuild(binding.getValue()))
                continue;
            
            Object key = binding.getKey();
            if (daoDependentKeys.contains(key)) {
                // This key (or some of its dependencies) depends on a dao session,
                // so it can only be constructed at the session container level
                if (binding.getValue() instanceof BuilderAdapter)
                    throw new IllegalStateException("Binding relying on a Builder cannot depend on a DAO");
                
                sessionBindings.put(key, binding.getValue());
            } else {
                // This key does not depend on a dao session so it can be
                // configured at the recommender container level
                if (binding.getValue() instanceof BuilderAdapter) {
                    // This was a built type, so bind to the built instance
                    // The buildContainer caches instances so this is a cheap lookup
                    recommenderContainer.addComponent(key, buildContainer.getComponent(key));
                } else {
                    // This wasn't meant to be built so configure binding again
                    recommenderContainer.addComponent(key, binding.getValue());
                }
            }
        }
        
        // Add additional configuration for the built instances that were JIT bound
        for (Entry<Object, BuilderAdapter<?>> jitBinding: jitBuilderFactory.jitBuilderAdapters.entrySet()) {
            if (!isBindingValidAfterBuild(jitBinding.getValue()))
                continue;
            
            if (daoDependentKeys.contains(jitBinding.getKey()))
                throw new IllegalStateException("Binding relying on a Builder cannot depend on a DAO");
            // As above, the built instance should be memoized so this is very fast
            recommenderContainer.addComponent(jitBinding.getKey(), buildContainer.getComponent(jitBinding.getKey()));
        }

        DAOFactory<? extends RatingDataAccessObject> manager =
            useManager ? daoManager : null;       
        RecommenderEngine engine = new LenskitRecommenderEngine(manager, recommenderContainer, sessionBindings);
        Recommender testOpen;
        if (useManager)
            testOpen = engine.open();
        else
            testOpen = engine.open(dao, false);
        testOpen.close();
        
        return engine;
    }
    
    private boolean isBindingValidAfterBuild(Object value) {
        Class<?> implType = null;
        if (value instanceof BuilderAdapter) { 
            implType = ((BuilderAdapter<?>) value).getComponentImplementation();
        } else if (value instanceof Class) {
            implType = (Class<?>) value;
        } else {
            implType = value.getClass();
        }
        
        // Do not configure any builders, snapshots or daos
        if (RatingSnapshot.class.isAssignableFrom(implType) || RatingDataAccessObject.class.isAssignableFrom(implType)
            || Builder.class.isAssignableFrom(implType)) {
            return false;
        }
        
        // Also check if it is a built type declared as ephemeral
        Built built = implType.getAnnotation(Built.class);
        if (built != null && built.ephemeral())
            return false;

        return true;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map<Object, Object> generateBindings(Map<Class<? extends Annotation>, Object> annotationBindings,
                                                 Map<Class<?>, Object> defaultBindings) {
        Map<Object, Object> keyBindings = new HashMap<Object, Object>();
        
        // Configure annotation bound types
        for (Entry<Class<? extends Annotation>, Object> paramBinding: annotationBindings.entrySet()) {
            Object value = paramBinding.getValue();
            Class implType = null;
            boolean usesBuilder = false;
            
            if (value instanceof Class) {
                if (Builder.class.isAssignableFrom((Class) value)) {
                    implType = getBuiltType((Class) value);
                    usesBuilder = true;
                } else {
                    implType = (Class) value;
                }
            } else {
                if (value instanceof Builder) {
                    implType = getBuiltType((Class) value.getClass());
                    usesBuilder = true;
                } else {
                    implType = value.getClass();
                }
            }
            
            implType = PrimitiveUtils.box(implType);
            
            // Walk up the type tree, creating bindings for every intermediate type
            // to allow for more specific injection points
            // FIXME: I don't think that this loop is sufficient for tree hierarchies that
            // involve subinterfaces (i.e. RatingPredictor and DynamicRatingPredictor)
            Class interfaceType = PrimitiveUtils.box(Parameters.getParameterType(paramBinding.getKey()));
            while(implType != null && interfaceType.isAssignableFrom(implType)) {
                BindKey key = new BindKey(implType, paramBinding.getKey());
                if (!usesBuilder) {
                    keyBindings.put(key, value);
                } else if (value instanceof Class) {
                    keyBindings.put(key, new BuilderAdapter(key, (Class) value));
                } else {
                    keyBindings.put(key, new BuilderAdapter(key, (Builder) value));
                }
                    
                implType = implType.getSuperclass();
                if (implType != null && implType.equals(Object.class))
                    implType = interfaceType;
            }
        }
        
        // Configure type-to-type bindings
        for (Entry<Class<?>, Object> dfltBinding: defaultBindings.entrySet()) {
            Object key = dfltBinding.getKey();
            Object value = dfltBinding.getValue();
            
            if (value instanceof Class) {
                if (Builder.class.isAssignableFrom((Class) value))
                    keyBindings.put(key, new BuilderAdapter(key, (Class) value));
                else
                    keyBindings.put(key, value);
            } else {
                if (value instanceof Builder)
                    keyBindings.put(key, new BuilderAdapter(key, (Builder) value));
                else
                    keyBindings.put(key, value);
            }
        }
        
//        for (Entry<Object, Object> b: keyBindings.entrySet()) {
//            Class annot = (b.getKey() instanceof BindKey ? ((BindKey) b.getKey()).getAnnotation() : null);
//            Class type = (b.getKey() instanceof BindKey ? ((BindKey) b.getKey()).getType() : (Class) b.getKey());
//            if (type.isInterface() || type.getSuperclass().equals(Object.class)) {
//                String key = (annot == null ? type.getSimpleName() : annot.getSimpleName() + ":" + type.getSimpleName());
//                String value = (b.getValue() instanceof Class ? ((Class) b.getValue()).getSimpleName() : b.getValue().toString());
//                System.out.println("Bind " + key + " -> " + value);
//            }
//        }
        return keyBindings;
    }
    
    @SuppressWarnings("unchecked")
    private static <T> Class<? extends Builder<? extends T>> findBuilder(Class<? extends T> type) {
        // Special handling for null types, must return a null builder so binding is still removed
        if (type == null)
            return null;
        
        // Convention #1: Type has been annotated with DefaultBuilder
        DefaultBuilder dfltBuilder = type.getAnnotation(DefaultBuilder.class);
        if (dfltBuilder != null) {
            // Type has been annotated with a default builder, so use that
            return (Class<? extends Builder<? extends T>>) dfltBuilder.value();
        }
        
        // Convention #2: Type has an static inner class that is a Builder of the appropriate type
        for (Class<?> cls: type.getClasses()) {
            if (Modifier.isStatic(cls.getModifiers()) && Builder.class.isAssignableFrom(cls)) {
                // cls is a static Builder, but make sure its built type is compatible
                if (type.isAssignableFrom(getBuiltType((Class<? extends Builder<?>>) cls)))
                    return (Class<? extends Builder<? extends T>>) cls;
            }
        }
        
        // Convention #3: There is a type named XBuilder in the same package as X
        String builderName = type.getName() + "Builder";
        try {
            Class<?> builderType = type.getClassLoader().loadClass(builderName);
            if (Builder.class.isAssignableFrom(builderType) 
                && type.isAssignableFrom(getBuiltType((Class<? extends Builder<?>>) builderType)))
                return (Class<? extends Builder<? extends T>>) builderType;
        } catch (ClassNotFoundException e) {
            // do nothing, a Builder wasn't found so throw an exception after leaving this block
        }
        
        throw new IllegalArgumentException("Unable to find a Builder for type: " + type);
    }
    
    private static Class<?> getBuiltType(Class<? extends Builder<?>> buildType) {
        try {
            // Builders are expected to update the return type of their build()
            // so we can get the actual built type by looking at the method
            return buildType.getMethod("build").getReturnType();
        } catch (Exception e) {
            // This shouldn't happen
            throw new RuntimeException(e);
        }
    }
    
    private static class BuilderTrackingAdapterFactory extends AbstractInjectionFactory {
        private static final long serialVersionUID = 1L;

        transient Map<Object, BuilderAdapter<?>> jitBuilderAdapters;
        transient InjectionFactory delegate;
        
        public BuilderTrackingAdapterFactory(InjectionFactory delegate) {
            this.delegate = delegate;
            jitBuilderAdapters = new HashMap<Object, BuilderAdapter<?>>();
        }
        
        @Override
        public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor componentMonitor,
                                                              LifecycleStrategy lifecycleStrategy,
                                                              Properties componentProperties, Object componentKey,
                                                              Class<T> componentImplementation, Parameter... parameters) throws PicoCompositionException {
            // Check to see if the type must be built
            if (componentImplementation.getAnnotation(Built.class) != null) {
                // Yes, so find a builder implementation and create an adapter for it
                Class<? extends Builder<? extends T>> builderType = findBuilder(componentImplementation);
                BuilderAdapter<T> adapter = new BuilderAdapter<T>(componentKey, builderType);
                
                jitBuilderAdapters.put(componentKey, adapter);
                return adapter;
            } else {
                // A regular jit binding so use the delegate
                return delegate.createComponentAdapter(componentMonitor, lifecycleStrategy, componentProperties, 
                                                       componentKey, componentImplementation, parameters);
            }
        }
    }
}
