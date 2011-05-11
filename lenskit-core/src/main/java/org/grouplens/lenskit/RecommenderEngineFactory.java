package org.grouplens.lenskit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.lenskit.data.LongCursor;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.SortOrder;
import org.grouplens.lenskit.data.UserRatingProfile;
import org.grouplens.lenskit.data.context.PackedRatingBuildContext;
import org.grouplens.lenskit.data.context.RatingBuildContext;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.params.meta.Built;
import org.grouplens.lenskit.params.meta.DefaultBuilder;
import org.grouplens.lenskit.params.meta.Parameters;
import org.grouplens.lenskit.pico.BuilderAdapter;
import org.grouplens.lenskit.pico.DependencyMonitor;
import org.grouplens.lenskit.pico.JustInTimePicoContainer;
import org.grouplens.lenskit.pico.ParameterAnnotationInjector;
import org.picocontainer.BindKey;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.InjectionFactory;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.injectors.AbstractInjectionFactory;
import org.picocontainer.lifecycle.StartableLifecycleStrategy;

@ThreadSafe
public class RecommenderEngineFactory {
    private final Map<Class<? extends Annotation>, Object> annotationBindings;
    private final Map<Class<?>, Class<?>> defaultBindings;
    
    public RecommenderEngineFactory(Class<? extends Builder<? extends RatingDataAccessObject>> daoBuilderClass) {
        annotationBindings = new HashMap<Class<? extends Annotation>, Object>();
        defaultBindings = new HashMap<Class<?>, Class<?>>();
        
        bindDefault(RatingBuildContext.class, PackedRatingBuildContext.class);
        bindDefaultBuilder(RatingDataAccessObject.class, daoBuilderClass);
    }
    
    public synchronized void setRecommender(Class<? extends Recommender> type) {
        if (type == null)
            throw new NullPointerException("Recommender type cannot be null");
        
        bindDefault(Recommender.class, type);
    }
    
    public synchronized void bind(Class<? extends Annotation> param, Number constant) {
        validateAnnotation(param);
        if (constant != null) {
            // Verify that the number is the proper primitive type
            Class<?> paramType = Parameters.getParameterType(param);
            
            // For now we'll do exact type matching and not worry about
            // float->double or int->long conversions
            if (!paramType.isInstance(constant))
                throw new IllegalArgumentException("Parameter " + param.getClass() + " expected a value of type " + paramType + ", not " + constant.getClass());
        }
        updateBindings(annotationBindings, param, constant);
    }
    
    public synchronized void bind(Class<? extends Annotation> param, Class<?> instanceType) {
        validateAnnotation(param);
        // Verify that the instance type is of the appropriate type
        Class<?> paramType = Parameters.getParameterType(param);
        if (instanceType != null && !paramType.isAssignableFrom(instanceType))
            throw new IllegalArgumentException(instanceType + " is incompatible with the type expected by parameter " + param.getClass() 
                                               + ", expected " + paramType);
        
        if (instanceType.getAnnotation(Built.class) != null) {
            bindBuilder(param, findBuilder(instanceType));
        } else {
            // This class can be created on its own
            updateBindings(annotationBindings, param, instanceType);
        }
    }
    
    public synchronized void bindBuilder(Class<? extends Annotation> param, Class<? extends Builder<?>> builderType) {
        validateAnnotation(param);
        if (builderType != null) {
            // Verify that the builder generates the appropriate type
            Class<?> paramType = Parameters.getParameterType(param);
            Class<?> builtType = getBuiltType(builderType);
            if (!paramType.isAssignableFrom(builtType))
                throw new IllegalArgumentException(builderType + " creates instances incompatible with the type expected by parameter " + param.getClass()
                                                   + ", expected " + paramType + ", but was " + builtType);
        }
        updateBindings(annotationBindings, param, builderType);
    }
    
    public synchronized <M> void bindDefault(Class<M> superType, Class<? extends M> instanceType) {
        if (superType == null)
            throw new NullPointerException("Super-type cannot be null");
        // Verify that the instanceType is actually a subtype
        if (instanceType != null && !superType.isAssignableFrom(instanceType))
            throw new IllegalArgumentException(instanceType + " is not a subclass of " + superType);
        
        if (instanceType.getAnnotation(Built.class) != null) {
            // Bind a builder instead
            bindDefaultBuilder(superType, findBuilder(instanceType));
        } else {
            // Type can be created on its own
            updateBindings(defaultBindings, superType, instanceType);
        }
    }
    
    public synchronized <M> void bindDefaultBuilder(Class<M> superType, Class<? extends Builder<? extends M>> builderType) {
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
            throw new NullPointerException("Annotation, param, cannot be null");
        
        if (!Parameters.isParameter(param))
            throw new IllegalArgumentException("Annotation, param, must be annotated with Parameter");
    }
    
    private <K, V> void updateBindings(Map<K, ? super V> bindings, K key, V value) {
        synchronized(this) {
            if (value == null)
                bindings.remove(key);
            else
                bindings.put(key, value);
        }
    }
    
    public RecommenderEngine create() {
        return create(null);
    }
    
    @SuppressWarnings("rawtypes")
    protected RecommenderEngine create(PicoContainer parent) {
        Map<Class<? extends Annotation>, Object> annotationBindings;
        Map<Class<?>, Class<?>> defaultBindings;
        
        synchronized(this) {
            // Clone configuration so that this build is thread safe
            annotationBindings = new HashMap<Class<? extends Annotation>, Object>(this.annotationBindings);
            defaultBindings = new HashMap<Class<?>, Class<?>>(this.defaultBindings);
        }
        
        DependencyMonitor daoMonitor = new DependencyMonitor(RatingDataAccessObject.class);
        BuilderTrackingAdapterFactory jitBuilderFactory = new BuilderTrackingAdapterFactory(new ParameterAnnotationInjector.Factory());
        MutablePicoContainer buildContainer = new JustInTimePicoContainer(jitBuilderFactory, new StartableLifecycleStrategy(daoMonitor),
                                                                          parent, daoMonitor);
        
        // We assume that these generated bindings include configurations for a dao and build context
        // and recommender types
        Map<Object, Object> keyBindings = generateBindings(annotationBindings, defaultBindings);

        // Push all bindings into the build container
        for (Entry<Object, Object> binding: keyBindings.entrySet()) {
            if (binding.getValue() instanceof ComponentAdapter)
                buildContainer.addAdapter((ComponentAdapter<?>) binding.getValue());
            else
                buildContainer.addComponent(binding.getKey(), binding.getValue());
        }
        
        // Construct all known objects to discover dependencies and to build things made by builders
        buildContainer.getComponents();
        Set<Object> daoDependentKeys = daoMonitor.getDependentKeys();
        
        // Close any opened dao
        RatingDataAccessObject buildSession = buildContainer.getComponent(RatingDataAccessObject.class);
        buildSession.close();
        
        // Create a new container that will be used by the RecommenderEngine.
        // This container will not contain builders or any of the components depending on a dao
        //  - built instances will be stored instead
        //  - dao dependencies are placed in a separate child container created per-session
        MutablePicoContainer recommenderContainer = new JustInTimePicoContainer(new ParameterAnnotationInjector.Factory(), parent);
        Map<Object, Object> sessionBindings = new HashMap<Object, Object>();
        
        // Configure recommender container with all bindings that don't depend on a dao
        for (Entry<Object, Object> binding: keyBindings.entrySet()) {
            Object key = binding.getKey();
            Class keyType = (key instanceof Class ? (Class) key : ((BindKey) key).getType());
            
            if (RatingBuildContext.class.isAssignableFrom(keyType) || RatingDataAccessObject.class.isAssignableFrom(keyType)
                || Builder.class.isAssignableFrom(keyType)) {
                // Do not configure any builders, contexts or daos
                continue;
            }
            
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
                    //  BuildAdapters memoize so this won't be expensive at all
                    recommenderContainer.addComponent(key, buildContainer.getComponent(key));
                } else {
                    // This wasn't meant to be built so configure binding again
                    recommenderContainer.addComponent(key, binding.getValue());
                }
            }
        }
        
        // Add additional configuration for the built instances that were JIT bound
        for (Entry<Object, BuilderAdapter<?>> jitBinding: jitBuilderFactory.jitBuilderAdapters.entrySet()) {
            if (daoDependentKeys.contains(jitBinding.getKey()))
                throw new IllegalStateException("Binding relying on a Builder cannot depend on a DAO");

            // As above, the built instance should be memoized so this is very fast
            recommenderContainer.addComponent(jitBinding.getKey(), buildContainer.getComponent(jitBinding.getKey()));
        }

        // Add the DAO binding in as a session binding
        sessionBindings.put(RatingDataAccessObject.class, keyBindings.get(RatingDataAccessObject.class));
        
        RecommenderEngine engine = new RecommenderEngineImpl(recommenderContainer, sessionBindings);
        Recommender testOpen = engine.open();
        testOpen.close();
        
        return engine;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map<Object, Object> generateBindings(Map<Class<? extends Annotation>, Object> annotationBindings,
                                                 Map<Class<?>, Class<?>> defaultBindings) {
        Map<Object, Object> keyBindings = new HashMap<Object, Object>();
        
        // Configure annotation bound types
        for (Entry<Class<? extends Annotation>, Object> paramBinding: annotationBindings.entrySet()) {
            Object value = paramBinding.getValue();
            Class implType = null;
            boolean usesBuilder = (paramBinding.getValue() instanceof Class &&
                                   Builder.class.isAssignableFrom((Class) value));
            
            if (paramBinding.getValue() instanceof Number)
                implType = paramBinding.getValue().getClass();
            else if (usesBuilder)
                implType = getBuiltType((Class) value);
            else
                implType = (Class<?>) paramBinding.getValue();
            
            
            // Walk up the type tree, creating bindings for every intermediate type
            // to allow for more specific injection points
            Class interfaceType = Parameters.getParameterType(paramBinding.getKey());
            while(interfaceType.isAssignableFrom(implType)) {
                BindKey key = new BindKey(implType, paramBinding.getKey());
                if (usesBuilder)
                    keyBindings.put(key, new BuilderAdapter(key, (Class) value));
                else
                    keyBindings.put(key, value);
                
                implType = implType.getSuperclass();
            }
        }
        
        // Configure type-to-type bindings
        for (Entry<Class<?>, Class<?>> dfltBinding: defaultBindings.entrySet()) {
            boolean usesBuilder = (dfltBinding.getValue() instanceof Class &&
                                   Builder.class.isAssignableFrom((Class) dfltBinding.getValue()));
            
            // We don't need to walk the type hierarchy in this case because PicoContainer
            // is smart enough to do that when asking for a component of just a type
            // (the first case was complicated since it used BindKeys)
            if (usesBuilder)
                keyBindings.put(dfltBinding.getKey(), new BuilderAdapter(dfltBinding.getKey(), (Class) dfltBinding.getValue()));
            else
                keyBindings.put(dfltBinding.getKey(), dfltBinding.getValue());
        }
        
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
    
    private static class RecommenderEngineImpl implements RecommenderEngine {
        private final PicoContainer recommenderContainer;
        private final Map<Object, Object> sessionBindings;
        
        public RecommenderEngineImpl(PicoContainer recommenderContainer, Map<Object, Object> sessionBindings) {
            this.recommenderContainer = recommenderContainer;
            this.sessionBindings = sessionBindings;
        }

        @Override
        public Recommender open() {
            return open(null);
        }

        @Override
        public Recommender open(RatingDataAccessObject dao, boolean shouldClose) {
            if (dao == null)
                throw new NullPointerException("Dao cannot be null when this method is used");
            return open(new CloseableDataAccessObjectWrapper(dao, shouldClose));
        }
        
        private Recommender open(RatingDataAccessObject dao) {
            MutablePicoContainer sessionContainer = new JustInTimePicoContainer(new ParameterAnnotationInjector.Factory(), 
                                                                                recommenderContainer);

            // Configure session container
            for (Entry<Object, Object> binding: sessionBindings.entrySet()) {
                Object value = binding.getValue();
                if ((value instanceof Class && RatingDataAccessObject.class.isAssignableFrom((Class<?>) value))
                    || value instanceof RatingDataAccessObject) {
                    if (dao != null) {
                        // Another dao has been provided, so use that as a binding
                        sessionContainer.addComponent(binding.getKey(), dao);
                    } else {
                        // Bind dao like any other session component
                        sessionContainer.addComponent(binding.getKey(), value);
                    }
                } else {
                    // Regular session-level component
                    sessionContainer.addComponent(binding.getKey(), value);
                }
            }
            
            return sessionContainer.getComponent(Recommender.class);
        }
    }
    
    private static class CloseableDataAccessObjectWrapper implements RatingDataAccessObject {
        private final boolean shouldClose;
        private final RatingDataAccessObject delegate;
        
        public CloseableDataAccessObjectWrapper(RatingDataAccessObject delegate, boolean shouldClose) {
            this.delegate = delegate;
            this.shouldClose = shouldClose;
        }
        
        @Override
        public void close() {
            if (shouldClose)
                delegate.close();
        }

        @Override
        public LongCursor getUsers() {
            return delegate.getUsers();
        }

        @Override
        public int getUserCount() {
            return delegate.getUserCount();
        }

        @Override
        public LongCursor getItems() {
            return delegate.getItems();
        }

        @Override
        public int getItemCount() {
            return delegate.getItemCount();
        }

        @Override
        public Cursor<Rating> getRatings() {
            return delegate.getRatings();
        }

        @Override
        public Cursor<Rating> getRatings(SortOrder order) {
            return delegate.getRatings(order);
        }

        @Override
        public Cursor<UserRatingProfile> getUserRatingProfiles() {
            return delegate.getUserRatingProfiles();
        }

        @Override
        public Cursor<Rating> getUserRatings(long userId) {
            return delegate.getUserRatings(userId);
        }

        @Override
        public Cursor<Rating> getUserRatings(long userId, SortOrder order) {
            return delegate.getUserRatings(userId, order);
        }

        @Override
        public Cursor<Rating> getItemRatings(long itemId) {
            return delegate.getItemRatings(itemId);
        }

        @Override
        public Cursor<Rating> getItemRatings(long itemId, SortOrder order) {
            return delegate.getItemRatings(itemId, order);
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
