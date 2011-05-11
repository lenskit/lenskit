package org.grouplens.lenskit.pico;

import java.lang.reflect.Type;

import org.grouplens.lenskit.Builder;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.adapters.AbstractAdapter;
import org.picocontainer.injectors.AbstractInjector.CyclicDependencyException;

public class BuilderAdapter<T> extends AbstractAdapter<T> {
    private static final long serialVersionUID = 1L;

    private Class<? extends Builder<? extends T>> builderType;
    
    private Builder<? extends T> builder;
    
    private transient T builtInstance;
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
            if (builtInstance == null) {
                if (builder == null)
                    builder = container.getComponent(builderType);
                
                if (builder != null)
                    builtInstance = builder.build();
            }
            
            return builtInstance;
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
