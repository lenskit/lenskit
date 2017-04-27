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
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.tuple.Pair;
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
    private static final ConcurrentMap<Class<? extends AbstractBeanEntity>, Pair<AttributeSet,ImmutableList<Function<Object,Object>>>> cache =
            new ConcurrentHashMap<>();

    protected final AttributeSet attributes;
    protected final ImmutableList<Function<Object,Object>> methods;

    /**
     * Construct a bean entity.
     */
    protected AbstractBeanEntity(EntityType typ, long id) {
        super(typ, id);
        Pair<AttributeSet, ImmutableList<Function<Object, Object>>> res = lookupAttrs(getClass());
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
    public Collection<Attribute<?>> getAttributes() {
        return new AbstractCollection<Attribute<?>>() {
            @Override
            public Iterator<Attribute<?>> iterator() {
                return (Iterator) IntStream.range(0, attributes.size())
                                           .mapToObj(i -> {
                                               Object val = methods.get(i).apply(AbstractBeanEntity.this);
                                               if (val == null) {
                                                   return null;
                                               } else {
                                                   return Attribute.create((TypedName) attributes.getAttribute(i), val);
                                               }
                                           })
                                           .filter(Predicates.notNull())
                                           .iterator();
            }

            @Override
            public int size() {
                return attributes.size();
            }
        };
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
            Function<Object,Object> gf = methods.get(idx);
            return gf.apply(this);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public <T> T maybeGet(TypedName<T> name) {
        int idx = attributes.lookup(name, true);
        if (idx >= 0) {
            Function<Object,Object> gf = methods.get(idx);
            return (T) gf.apply(this);
        } else {
            return null;
        }
    }

    @Override
    public long getLong(TypedName<Long> name) {
        int idx = attributes.lookup(name);
        if (idx >= 0) {
            Function<Object,Object> gf = methods.get(idx);
            if (gf instanceof ToLongFunction) {
                return ((ToLongFunction) gf).applyAsLong(this);
            } else {
                return (long) gf.apply(this);
            }
        } else {
            throw new NoSuchAttributeException(name.toString());
        }
    }

    @Override
    public double getDouble(TypedName<Double> name) {
        int idx = attributes.lookup(name);
        if (idx >= 0) {
            Function<Object,Object> gf = methods.get(idx);
            if (gf instanceof ToLongFunction) {
                return ((ToDoubleFunction) gf).applyAsDouble(this);
            } else {
                return (double) gf.apply(this);
            }
        } else {
            throw new NoSuchAttributeException(name.toString());
        }
    }

    @Override
    public int getInteger(TypedName<Integer> name) {
        int idx = attributes.lookup(name);
        if (idx >= 0) {
            Function<Object,Object> gf = methods.get(idx);
            return (int) gf.apply(this);
        } else {
            throw new NoSuchAttributeException(name.toString());
        }
    }

    @Override
    public boolean getBoolean(TypedName<Boolean> name) {
        int idx = attributes.lookup(name);
        if (idx >= 0) {
            Function<Object,Object> gf = methods.get(idx);
            return (boolean) gf.apply(this);
        } else {
            throw new NoSuchAttributeException(name.toString());
        }
    }

    private static Pair<AttributeSet, ImmutableList<Function<Object,Object>>> lookupAttrs(Class<? extends AbstractBeanEntity> type) {
        Pair<AttributeSet, ImmutableList<Function<Object,Object>>> res = cache.get(type);
        if (res != null) {
            return res;
        }

        DynamicClassLoader dlc = new DynamicClassLoader(type.getClassLoader());
        Map<String,Function<Object,Object>> attrs = new HashMap<>();
        List<TypedName<?>> names = new ArrayList<>();
        for (Method m: type.getMethods()) {
            EntityAttribute annot = m.getAnnotation(EntityAttribute.class);
            if (annot != null) {
                Function<Object, Object> gfunc = generateGetter(dlc, type, m);
                attrs.put(annot.value(), gfunc);
                names.add(TypedName.create(annot.value(), TypeToken.of(m.getGenericReturnType())));
            }
        }

        AttributeSet aset = AttributeSet.create(names);
        ImmutableList.Builder<Function<Object,Object>> mhlb = ImmutableList.builder();
        for (String name: aset.nameSet()) {
            mhlb.add(attrs.get(name));
        }

        res = Pair.of(aset, mhlb.build());
        cache.put(type, res);
        return res;
    }

    private static Function<Object,Object> generateGetter(DynamicClassLoader dlc, Class<? extends AbstractBeanEntity> type, Method getter) {
        ClassNode node = new ClassNode();
        node.name = String.format("%s$$AttrGet$%s", Type.getInternalName(type), getter.getName());
        node.access = ACC_PUBLIC;
        node.version = V1_8;
        node.superName = Type.getInternalName(Object.class);
        node.interfaces = Lists.newArrayList(Type.getInternalName(Function.class));
        node.methods.add(generateGetterConstructor());
        node.methods.add(generateGetterMethod(type, getter));
        if (Type.getReturnType(getter).equals(Type.LONG_TYPE)) {
            node.methods.add(generateLongGetterMethod(type, getter));
            node.interfaces.add(Type.getInternalName(ToLongFunction.class));
        } else if (Type.getReturnType(getter).equals(Type.DOUBLE_TYPE)) {
            node.methods.add(generateDoubleGetterMethod(type, getter));
            node.interfaces.add(Type.getInternalName(ToDoubleFunction.class));
        }

        Class<? extends Function> cls = dlc.defineClass(node).asSubclass(Function.class);
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
        cn.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        cn.visitInsn(RETURN);
        return cn;
    }

    private static MethodNode generateGetterMethod(Class<? extends AbstractBeanEntity> type, Method getter) {
        MethodNode gn = new MethodNode();
        gn.name = "apply";
        gn.desc = "(Ljava/lang/Object;)Ljava/lang/Object;";
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
        gn.name = "applyAsLong";
        gn.desc = "(Ljava/lang/Object;)J";
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
        gn.name = "applyAsDouble";
        gn.desc = "(Ljava/lang/Object;)D";
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
