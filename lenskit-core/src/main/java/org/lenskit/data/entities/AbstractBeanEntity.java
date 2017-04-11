package org.lenskit.data.entities;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract entity implementation that uses bean methods.
 */
public abstract class AbstractBeanEntity extends AbstractEntity {
    private static final ConcurrentHashMap<Class<? extends AbstractBeanEntity>, Pair<AttributeSet,ImmutableList<MethodHandle>>> cache =
            new ConcurrentHashMap<>();

    protected final AttributeSet attributes;
    protected final ImmutableList<MethodHandle> methods;

    /**
     * Construct a bean entity.
     */
    protected AbstractBeanEntity(EntityType typ, long id) {
        super(typ, id);
        Pair<AttributeSet,ImmutableList<MethodHandle>> res = lookupAttrs(getClass());
        attributes = res.getLeft();
        methods = res.getRight();
    }

    @Override
    public Set<TypedName<?>> getTypedAttributeNames() {
        return attributes;
    }

    @Override
    public Set<String> getAttributeNames() {
        return attributes.nameSet();
    }

    @Override
    public boolean hasAttribute(String name) {
        return attributes.nameSet().contains(name);
    }

    @Nullable
    @Override
    public Object maybeGet(String attr) {
        int idx = attributes.lookup(attr);
        if (idx >= 0) {
            MethodHandle mh = methods.get(idx);
            try {
                return mh.invoke(this);
            } catch (Throwable t) {
                throw Throwables.propagate(t);
            }
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public <T> T maybeGet(TypedName<T> name) {
        int idx = attributes.lookup(name);
        if (idx >= 0) {
            MethodHandle mh = methods.get(idx);
            try {
                return (T) mh.invoke(this);
            } catch (Throwable t) {
                throw Throwables.propagate(t);
            }
        } else {
            return null;
        }
    }

    private static Pair<AttributeSet, ImmutableList<MethodHandle>> lookupAttrs(Class<? extends AbstractBeanEntity> type) {
        Pair<AttributeSet, ImmutableList<MethodHandle>> res = cache.get(type);
        if (res != null) {
            return res;
        }

        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        Map<String,MethodHandle> attrs = new HashMap<>();
        List<TypedName<?>> names = new ArrayList<>();
        for (Method m: type.getMethods()) {
            EntityAttribute annot = m.getAnnotation(EntityAttribute.class);
            if (annot != null) {
                try {
                    attrs.put(annot.value(), lookup.unreflect(m));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("cannot access " + m);
                }
                names.add(TypedName.create(annot.value(), TypeToken.of(m.getGenericReturnType())));
            }
        }

        AttributeSet aset = AttributeSet.create(names);
        ImmutableList.Builder<MethodHandle> mhlb = ImmutableList.builder();
        for (String name: aset.nameSet()) {
            mhlb.add(attrs.get(name));
        }

        return Pair.of(aset, mhlb.build());
    }
}
