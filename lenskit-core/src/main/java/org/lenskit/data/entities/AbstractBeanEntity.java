package org.lenskit.data.entities;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract entity implementation that uses bean methods.
 */
public abstract class AbstractBeanEntity extends AbstractEntity {
    private static final ConcurrentHashMap<Class<? extends AbstractBeanEntity>, Pair<AttributeSet,ImmutableList<Method>>> cache =
            new ConcurrentHashMap<>();

    protected final AttributeSet attributes;
    protected final ImmutableList<Method> methods;

    /**
     * Construct a bean entity.
     */
    protected AbstractBeanEntity(EntityType typ, long id) {
        super(typ, id);
        Pair<AttributeSet,ImmutableList<Method>> res = lookupAttrs(getClass());
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

    @Override
    public boolean hasAttribute(TypedName<?> name) {
        return attributes.contains(name);
    }

    @Nullable
    @Override
    public Object maybeGet(String attr) {
        int idx = attributes.lookup(attr);
        if (idx >= 0) {
            Method mh = methods.get(idx);
            try {
                return mh.invoke(this);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Error invoking " + mh, e);
            }
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public <T> T maybeGet(TypedName<T> name) {
        int idx = attributes.lookup(name, true);
        if (idx >= 0) {
            Method mh = methods.get(idx);
            try {
                return (T) mh.invoke(this);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Error invoking " + mh, e);
            }
        } else {
            return null;
        }
    }

    @Override
    public long getLong(TypedName<Long> name) {
        int idx = attributes.lookup(name);
        if (idx >= 0) {
            Method mh = methods.get(idx);
            try {
                return (long) mh.invoke(this);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Error invoking " + mh, e);
            }
        } else {
            throw new NoSuchAttributeException(name.toString());
        }
    }

    @Override
    public double getDouble(TypedName<Double> name) {
        int idx = attributes.lookup(name);
        if (idx >= 0) {
            Method mh = methods.get(idx);
            try {
                return (double) mh.invoke(this);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Error invoking " + mh, e);
            }
        } else {
            throw new NoSuchAttributeException(name.toString());
        }
    }

    @Override
    public int getInteger(TypedName<Integer> name) {
        int idx = attributes.lookup(name);
        if (idx >= 0) {
            Method mh = methods.get(idx);
            try {
                return (int) mh.invoke(this);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Error invoking " + mh, e);
            }
        } else {
            throw new NoSuchAttributeException(name.toString());
        }
    }

    @Override
    public boolean getBoolean(TypedName<Boolean> name) {
        int idx = attributes.lookup(name);
        if (idx >= 0) {
            Method mh = methods.get(idx);
            try {
                return (boolean) mh.invoke(this);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Error invoking " + mh, e);
            }
        } else {
            throw new NoSuchAttributeException(name.toString());
        }
    }

    private static Pair<AttributeSet, ImmutableList<Method>> lookupAttrs(Class<? extends AbstractBeanEntity> type) {
        Pair<AttributeSet, ImmutableList<Method>> res = cache.get(type);
        if (res != null) {
            return res;
        }

        Map<String,Method> attrs = new HashMap<>();
        List<TypedName<?>> names = new ArrayList<>();
        for (Method m: type.getMethods()) {
            EntityAttribute annot = m.getAnnotation(EntityAttribute.class);
            if (annot != null) {
                m.setAccessible(true);
                attrs.put(annot.value(), m);
                names.add(TypedName.create(annot.value(), TypeToken.of(m.getGenericReturnType())));
            }
        }

        AttributeSet aset = AttributeSet.create(names);
        ImmutableList.Builder<Method> mhlb = ImmutableList.builder();
        for (String name: aset.nameSet()) {
            mhlb.add(attrs.get(name));
        }

        res = Pair.of(aset, mhlb.build());
        cache.put(type, res);
        return res;
    }
}
