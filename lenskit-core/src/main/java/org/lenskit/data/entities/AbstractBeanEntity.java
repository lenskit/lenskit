/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.data.entities;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import org.lenskit.util.reflect.CGUtils;
import org.lenskit.util.reflect.DynamicClassLoader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;
import java.util.stream.IntStream;

import static org.objectweb.asm.Opcodes.*;

/**
 * Abstract entity implementation that uses bean methods.
 */
public abstract class AbstractBeanEntity extends AbstractEntity {
    private static final ConcurrentMap<Class<? extends AbstractBeanEntity>, BeanEntityLayout> cache =
            new ConcurrentHashMap<>();

    private final BeanEntityLayout layout;

    /**
     * Construct a bean entity.
     *
     * @param bel The layout (from {@link #makeLayout(Class)}).
     * @param typ The entity type
     * @param id The entity ID.
     */
    protected AbstractBeanEntity(BeanEntityLayout bel, EntityType typ, long id) {
        super(typ, id);
        layout = bel;
    }

    @Override
    public Set<TypedName<?>> getTypedAttributeNames() {
        return layout.attributes;
    }

    @Override
    public Set<String> getAttributeNames() {
        return layout.attributes.nameSet();
    }

    @Override
    public Collection<Attribute<?>> getAttributes() {
        return new AbstractCollection<Attribute<?>>() {
            @Override
            public Iterator<Attribute<?>> iterator() {
                return (Iterator) IntStream.range(0, layout.attributes.size())
                                           .mapToObj(i -> {
                                               Object val = layout.getters.get(i).get(AbstractBeanEntity.this);
                                               if (val == null) {
                                                   return null;
                                               } else {
                                                   return Attribute.create((TypedName) layout.attributes.getAttribute(i), val);
                                               }
                                           })
                                           .filter(Predicates.notNull())
                                           .iterator();
            }

            @Override
            public int size() {
                return layout.attributes.size();
            }
        };
    }

    @Override
    public boolean hasAttribute(String name) {
        return layout.attributes.nameSet().contains(name);
    }

    @Override
    public boolean hasAttribute(TypedName<?> name) {
        return layout.attributes.contains(name);
    }

    @Nullable
    @Override
    public Object maybeGet(String attr) {
        int idx = layout.attributes.lookup(attr);
        if (idx >= 0) {
            BeanAttributeGetter gf = layout.getters.get(idx);
            return gf.get(this);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public <T> T maybeGet(TypedName<T> name) {
        int idx = layout.attributes.lookup(name, true);
        if (idx >= 0) {
            BeanAttributeGetter gf = layout.getters.get(idx);
            return (T) gf.get(this);
        } else {
            return null;
        }
    }

    @Override
    public long getLong(TypedName<Long> name) {
        int idx = layout.attributes.lookup(name);
        if (idx >= 0) {
            BeanAttributeGetter gf = layout.getters.get(idx);
            return gf.getLong(this);
        } else {
            throw new NoSuchAttributeException(name.toString());
        }
    }

    @Override
    public double getDouble(TypedName<Double> name) {
        int idx = layout.attributes.lookup(name);
        if (idx >= 0) {
            BeanAttributeGetter gf = layout.getters.get(idx);
            return gf.getDouble(this);
        } else {
            throw new NoSuchAttributeException(name.toString());
        }
    }

    @Override
    public int getInteger(TypedName<Integer> name) {
        int idx = layout.attributes.lookup(name);
        if (idx >= 0) {
            BeanAttributeGetter gf = layout.getters.get(idx);
            return gf.getInt(this);
        } else {
            throw new NoSuchAttributeException(name.toString());
        }
    }

    @Override
    public boolean getBoolean(TypedName<Boolean> name) {
        int idx = layout.attributes.lookup(name);
        if (idx >= 0) {
            BeanAttributeGetter gf = layout.getters.get(idx);
            return gf.getBoolean(this);
        } else {
            throw new NoSuchAttributeException(name.toString());
        }
    }

    protected static class BeanEntityLayout {
        private final AttributeSet attributes;
        private final ImmutableList<BeanAttributeGetter> getters;

        BeanEntityLayout(AttributeSet as, ImmutableList<BeanAttributeGetter> gs) {
            attributes = as;
            getters = gs;
        }
    }

    /**
     * Internal utility class - do not use.
     */
    public static abstract class BeanAttributeGetter {
        public abstract Object get(AbstractBeanEntity bean);

        public long getLong(AbstractBeanEntity bean) {
            return (long) get(bean);
        }

        public double getDouble(AbstractBeanEntity bean) {
            return (double) get(bean);
        }

        public int getInt(AbstractBeanEntity bean) {
            return (int) get(bean);
        }

        public boolean getBoolean(AbstractBeanEntity bean) {
            return (boolean) get(bean);
        }
    }

    protected static BeanEntityLayout makeLayout(Class<? extends AbstractBeanEntity> type) {
        BeanEntityLayout res = cache.get(type);
        if (res != null) {
            return res;
        }

        DynamicClassLoader dlc = new DynamicClassLoader(type.getClassLoader());
        Map<String, BeanAttributeGetter> attrs = new HashMap<>();
        List<TypedName<?>> names = new ArrayList<>();
        for (Method m: type.getMethods()) {
            EntityAttribute annot = m.getAnnotation(EntityAttribute.class);
            if (annot != null) {
                BeanAttributeGetter gfunc = generateGetter(dlc, type, m);
                attrs.put(annot.value(), gfunc);
                names.add(TypedName.create(annot.value(), TypeToken.of(m.getGenericReturnType())));
            }
        }

        AttributeSet aset = AttributeSet.create(names);
        ImmutableList.Builder<BeanAttributeGetter> mhlb = ImmutableList.builder();
        for (String name: aset.nameSet()) {
            mhlb.add(attrs.get(name));
        }

        res = new BeanEntityLayout(aset, mhlb.build());
        cache.put(type, res);
        return res;
    }

    private static BeanAttributeGetter generateGetter(DynamicClassLoader dlc, Class<? extends AbstractBeanEntity> type, Method getter) {
        ClassNode node = new ClassNode();
        node.name = String.format("%s$$AttrGet$%s", Type.getInternalName(type), getter.getName());
        node.access = ACC_PUBLIC;
        node.version = V1_8;
        node.superName = Type.getInternalName(BeanAttributeGetter.class);
        node.methods.add(generateGetterConstructor());
        node.methods.add(generateGetterMethod(type, getter));
        if (Type.getReturnType(getter).equals(Type.LONG_TYPE)) {
            node.methods.add(generateLongGetterMethod(type, getter));
        } else if (Type.getReturnType(getter).equals(Type.DOUBLE_TYPE)) {
            node.methods.add(generateDoubleGetterMethod(type, getter));
        }

        Class<? extends BeanAttributeGetter> cls = dlc.defineClass(node).asSubclass(BeanAttributeGetter.class);
        try {
            return cls.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Cannot instantiate " + cls, e);
        }
    }

    private static MethodNode generateGetterConstructor() {
        MethodNode cn = new MethodNode();
        cn.name = "<init>";
        cn.desc = "()V";
        cn.access = ACC_PUBLIC;
        cn.exceptions = Collections.emptyList();
        cn.maxStack = 1;
        cn.maxLocals = 1;
        cn.visitVarInsn(ALOAD, 0);
        cn.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(BeanAttributeGetter.class),
                           "<init>", "()V", false);
        cn.visitInsn(RETURN);
        return cn;
    }

    private static MethodNode generateGetterMethod(Class<? extends AbstractBeanEntity> type, Method getter) {
        MethodNode gn = new MethodNode();
        gn.name = "get";
        gn.desc = Type.getMethodDescriptor(Type.getType(Object.class),
                                           Type.getType(AbstractBeanEntity.class));
        gn.access = ACC_PUBLIC;
        gn.exceptions = Collections.emptyList();
        Type rt = Type.getReturnType(getter);
        gn.maxLocals = 2;
        gn.maxStack = 1 + rt.getSize();
        gn.visitCode();
        gn.visitVarInsn(ALOAD, 1);
        gn.visitTypeInsn(CHECKCAST, Type.getInternalName(type));
        gn.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(type),
                           getter.getName(), Type.getMethodDescriptor(getter), false);
        CGUtils.adaptFromType(gn, getter.getReturnType());
        gn.visitInsn(ARETURN);
        return gn;
    }

    private static MethodNode generateLongGetterMethod(Class<? extends AbstractBeanEntity> type, Method getter) {
        MethodNode gn = new MethodNode();
        gn.name = "getLong";
        gn.desc = Type.getMethodDescriptor(Type.LONG_TYPE, Type.getType(AbstractBeanEntity.class));
        gn.access = ACC_PUBLIC;
        gn.exceptions = Collections.emptyList();
        gn.maxLocals = 2;
        gn.maxStack = 2;
        gn.visitCode();
        gn.visitVarInsn(ALOAD, 1);
        gn.visitTypeInsn(CHECKCAST, Type.getInternalName(type));
        gn.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(type),
                           getter.getName(), Type.getMethodDescriptor(getter), false);
        gn.visitInsn(LRETURN);
        return gn;
    }

    private static MethodNode generateDoubleGetterMethod(Class<? extends AbstractBeanEntity> type, Method getter) {
        MethodNode gn = new MethodNode();
        gn.name = "getDouble";
        gn.desc = Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.getType(AbstractBeanEntity.class));
        gn.access = ACC_PUBLIC;
        gn.exceptions = Collections.emptyList();
        gn.maxLocals = 2;
        gn.maxStack = 2;
        gn.visitCode();
        gn.visitVarInsn(ALOAD, 1);
        gn.visitTypeInsn(CHECKCAST, Type.getInternalName(type));
        gn.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(type),
                           getter.getName(), Type.getMethodDescriptor(getter), false);
        gn.visitInsn(DRETURN);
        return gn;
    }
}
