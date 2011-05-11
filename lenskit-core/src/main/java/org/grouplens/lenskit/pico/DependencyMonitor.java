package org.grouplens.lenskit.pico;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

import org.picocontainer.Behavior;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.Injector;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;

public class DependencyMonitor implements ComponentMonitor {
    private Class<?> monitorType;
    
    private transient Set<Object> keysDependingOnMonitor;
    private transient WeakHashMap<Object, Boolean> dependentInstances;
    
    public DependencyMonitor(Class<?> typeToMonitor) {
        monitorType = typeToMonitor;
    }
    
    public Set<Object> getDependentKeys() {
        synchronized(this) {
            if (keysDependingOnMonitor == null)
                return Collections.emptySet();
            else
                return Collections.unmodifiableSet(new HashSet<Object>(keysDependingOnMonitor));
        }
    }
    
    @Override
    public <T> void instantiated(PicoContainer container, ComponentAdapter<T> componentAdapter,
                                 Constructor<T> constructor, Object instantiated,
                                 Object[] injected, long duration) {
        updateDependencies(instantiated, componentAdapter, injected);
    }
    
    @Override
    public void invoked(PicoContainer container, ComponentAdapter<?> componentAdapter, Member member,
                        Object instance, long duration, Object[] args, Object retVal) {
        updateDependencies(instance, componentAdapter, args);
    }
    
    private void updateDependencies(Object instance, ComponentAdapter<?> adapter, Object[] injected) {
        synchronized(this) {
            if (keysDependingOnMonitor == null)
                keysDependingOnMonitor = new HashSet<Object>();
            if (dependentInstances == null)
                dependentInstances = new WeakHashMap<Object, Boolean>();
            
            Object key = adapter.getComponentKey();
            if (keysDependingOnMonitor.contains(key)) {
                // No need to examine dependencies, but make sure its in the instance map
                dependentInstances.put(instance, Boolean.TRUE);
                return;
            }
            
            for (Object injectee: injected) {
                if (dependentInstances.containsKey(injectee) || monitorType.isInstance(injectee)) {
                    // Store the adapter key and the created instance.
                    // The created instance is stored because the monitor isn't provided the keys
                    //  of the injected parameters (so we use the instances to look at transitive dependencies)
                    dependentInstances.put(instance, Boolean.TRUE);
                    keysDependingOnMonitor.add(key);
                    
                    // Once we've placed the instantiated object in the tracking structures,
                    // there's no need to continue
                    break;
                }
            }
        }
    }

    @Override
    public <T> void instantiationFailed(PicoContainer container,
                                        ComponentAdapter<T> componentAdapter,
                                        Constructor<T> constructor, Exception cause) {
        // do nothing
    }
    
    @Override
    public void invocationFailed(Member member, Object instance, Exception cause) {
        // do nothing
    }
    
    @Override
    public <T> Constructor<T> instantiating(PicoContainer container,
                                            ComponentAdapter<T> componentAdapter,
                                            Constructor<T> constructor) {
        // do nothing, really
        return constructor;
    }

    @Override
    public Object invoking(PicoContainer container, ComponentAdapter<?> componentAdapter,
                           Member member, Object instance, Object[] args) {
        // do nothing, really
        return KEEP;
    }

    @Override
    public void lifecycleInvocationFailed(MutablePicoContainer container,
                                          ComponentAdapter<?> componentAdapter, Method method,
                                          Object instance, RuntimeException cause) {
        // do nothing
    }

    @Override
    public Object noComponentFound(MutablePicoContainer container, Object componentKey) {
        // do nothing, JIT is supported directly in JustInTimePicoContainer
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Injector newInjector(Injector injector) {
        return injector;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Behavior newBehavior(Behavior behavior) {
        return behavior;
    }
}
