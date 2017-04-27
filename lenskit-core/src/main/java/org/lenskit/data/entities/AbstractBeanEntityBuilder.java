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

import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.lenskit.util.reflect.DynamicClassLoader;
import org.objectweb.asm.tree.*;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * Base class for entity builders using bean-style setters.  An entity builder should extend this class, then annotate
 * setter methods with {@link EntityAttributeSetter} (and clearers with {@link EntityAttributeClearer}).  The standard
 * methods will then be implemented in terms of these.
 */
public class AbstractBeanEntityBuilder extends EntityBuilder {
    private static final ConcurrentHashMap<Class<? extends AbstractBeanEntityBuilder>, AttrMethod[]> cache =
            new ConcurrentHashMap<>();

    private final AttrMethod[] attributeRecords;

    protected AbstractBeanEntityBuilder(EntityType type) {
        super(type);
        attributeRecords = lookupAttrs(getClass());
    }

    @Nullable
    private final AttrMethod findEntry(TypedName<?> name) {
        for (AttrMethod e: attributeRecords) {
            if (e.name == name) {
                return e;
            }
        }

        return null;
    }

    @Override
    public <T> EntityBuilder setAttribute(TypedName<T> name, T val) {
        AttrMethod e = findEntry(name);
        if (e == null) {
            setExtraAttribute(name, val);
        } else {
            e.set(this, val);
        }

        return this;
    }

    @Override
    public EntityBuilder clearAttribute(TypedName<?> name) {
        AttrMethod e = findEntry(name);

        if (e == null) {
            clearExtraAttribute(name);
        } else {
            e.clear(this);
        }

        return this;
    }

    /**
     * Set an extra attribute. An extra attribute is an attribute that is not provided by a bean-style setter.
     *
     * The default implementation throws {@link NoSuchAttributeException}.
     *
     * @param name The attribute name to clear.
     * @param val The attribute value to set.
     */
    public <T> void setExtraAttribute(TypedName<T> name, T val) {
        throw new NoSuchAttributeException(name.toString());
    }

    /**
     * Clear an extra attribute. An extra attribute is an attribute that is not provided by a bean-style setter.
     *
     * The default implementation does nothing.
     *
     * @param name The attribute name to clear.
     */
    public void clearExtraAttribute(TypedName<?> name) {

    }

    @Override
    public Entity build() {
        return null;
    }

    private static AttrMethod[] lookupAttrs(Class<? extends AbstractBeanEntityBuilder> type) {
        AttrMethod[] res = cache.get(type);
        if (res != null) {
            return res;
        }

        DynamicClassLoader dlc = new DynamicClassLoader(type.getClassLoader());
        Map<TypedName<?>,Method> setters = new HashMap<>();
        Map<String,Method> clearers = new HashMap<>();
        for (Method m: type.getMethods()) {
            EntityAttributeSetter annot = m.getAnnotation(EntityAttributeSetter.class);
            if (annot != null) {
                Type[] params = m.getParameterTypes();
                if (params.length != 1) {
                    throw new IllegalArgumentException("method " + m + " has " + params.length + " parameters, expected 1");
                }
                TypeToken atype = TypeToken.of(params[0]);
                TypedName<?> name = TypedName.create(annot.value(), atype);
                setters.put(name, m);
            }
            EntityAttributeClearer clearAnnot = m.getAnnotation(EntityAttributeClearer.class);
            if (clearAnnot != null) {
                clearers.put(clearAnnot.value(), m);
            }
        }

        AttrMethod[] ael = new AttrMethod[setters.size()];
        int attrIdx = 0;
        for (Map.Entry<TypedName<?>,Method> ce: setters.entrySet()) {
            TypedName<?> attr = ce.getKey();
            Method smethod = ce.getValue();
            Method cmethod = clearers.get(ce.getKey().getName());
            ClassNode cn = new ClassNode();
            cn.name = String.format("org/lenskit/generated/entities/AttrSet$$%s$$%s",
                                    type.getName().replace('.', '$'),
                                    ce.getValue().getName());
            cn.access = ACC_PUBLIC;
            cn.version = V1_8;
            cn.superName = getInternalName(AttrMethod.class);
            MethodNode ctor = new MethodNode();
            ctor.access = ACC_PUBLIC;
            ctor.desc = getMethodDescriptor(VOID_TYPE, getType(TypedName.class));
            ctor.name = "<init>";
            ctor.exceptions = Collections.emptyList();
            ctor.maxLocals = 2;
            ctor.maxStack = 2;
            InsnList ctc = ctor.instructions;
            ctc.add(new VarInsnNode(ALOAD, 0));
            ctc.add(new VarInsnNode(ALOAD, 1));
            ctc.add(new MethodInsnNode(INVOKESPECIAL, getInternalName(AttrMethod.class),
                                       "<init>", ctor.desc, false));
            ctc.add(new InsnNode(RETURN));
            cn.methods.add(ctor);

            MethodNode setter = new MethodNode();
            setter.access = ACC_PUBLIC;
            setter.desc = getMethodDescriptor(VOID_TYPE,
                                              getType(AbstractBeanEntityBuilder.class),
                                              getType(Object.class));
            setter.name = "set";
            setter.exceptions = Collections.emptyList();
            setter.maxLocals = 3;
            setter.maxStack = 2;
            InsnList sis = setter.instructions;
            sis.add(new VarInsnNode(ALOAD, 1));
            sis.add(new TypeInsnNode(CHECKCAST, getInternalName(type)));
            sis.add(new VarInsnNode(ALOAD, 2));
            adaptType(setter, attr.getRawType(), smethod.getParameterTypes()[0]);
            sis.add(new MethodInsnNode(INVOKEVIRTUAL, getInternalName(type),
                                       smethod.getName(), getMethodDescriptor(smethod),
                                       false));
            sis.add(new InsnNode(RETURN));
            cn.methods.add(setter);

            MethodNode clearer = new MethodNode();
            clearer.access = ACC_PUBLIC;
            clearer.desc = getMethodDescriptor(VOID_TYPE,
                                              getType(AbstractBeanEntityBuilder.class));
            clearer.name = "clear";
            clearer.exceptions = Collections.emptyList();
            clearer.maxLocals = 2;
            clearer.maxStack = 1;
            InsnList cis = clearer.instructions;
            if (cmethod != null) {
                cis.add(new VarInsnNode(ALOAD, 1));
                cis.add(new TypeInsnNode(CHECKCAST, getInternalName(type)));
                cis.add(new MethodInsnNode(INVOKEVIRTUAL, getInternalName(type),
                                           cmethod.getName(), getMethodDescriptor(cmethod),
                                           false));
                cis.add(new InsnNode(RETURN));
            } else if (!smethod.getParameterTypes()[0].isPrimitive()) {
                cis.add(new VarInsnNode(ALOAD, 1));
                cis.add(new TypeInsnNode(CHECKCAST, getInternalName(type)));
                cis.add(new InsnNode(ACONST_NULL));
                clearer.maxStack = 2;
                cis.add(new MethodInsnNode(INVOKEVIRTUAL, getInternalName(type),
                                           smethod.getName(), getMethodDescriptor(smethod),
                                           false));
                cis.add(new InsnNode(RETURN));
            } else {
                clearer.maxStack = 2;
                cis.add(new TypeInsnNode(NEW, "java/lang/UnsupportedOperationException"));
                cis.add(new InsnNode(DUP));
                cis.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/UnsupportedOperationException", "<init>", "()V", false));
                cis.add(new InsnNode(ATHROW));
            }
            cn.methods.add(clearer);

            Class<? extends AttrMethod> cls = dlc.defineClass(cn).asSubclass(AttrMethod.class);
            try {
                ael[attrIdx++] = ConstructorUtils.invokeConstructor(cls, attr);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException("cannot instantiate " + cls, e);
            }
        }

        cache.put(type, ael);
        return ael;
    }

    private static void adaptType(MethodNode method, Class<?> rawType, Class<?> paramType) {
        if (paramType.isPrimitive()) {
            if (paramType.equals(long.class)) {
                method.instructions.add(new TypeInsnNode(CHECKCAST, getInternalName(Long.class)));
                method.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, getInternalName(Long.class),
                                          "longValue", "()J", false));
                method.maxStack += 1;
            } else if (paramType.equals(int.class)) {
                method.instructions.add(new TypeInsnNode(CHECKCAST, getInternalName(Integer.class)));
                method.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, getInternalName(Integer.class),
                                          "intValue", "()I", false));
            } else if (paramType.equals(double.class)) {
                method.instructions.add(new TypeInsnNode(CHECKCAST, getInternalName(Double.class)));
                method.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, getInternalName(Double.class),
                                          "doubleValue", "()D", false));
                method.maxStack += 1;
            } else {
                throw new IllegalArgumentException("type " + paramType + " not yet supported");
            }
        } else {
            method.instructions.add(new TypeInsnNode(CHECKCAST, getInternalName(paramType)));
        }
    }

    /**
     * Abstract class for accessing attribute methods.  This is only for internal use.
     */
    public abstract static class AttrMethod {
        final TypedName<?> name;

        protected AttrMethod(TypedName<?> n) {
            name = n;
        }

        public abstract void set(AbstractBeanEntityBuilder receiver, Object value);
        public abstract void clear(AbstractBeanEntityBuilder receiver);
    }

    private static class ReflectionAttrMethod extends AttrMethod {
        Method setter;
        Method clearer;

        public ReflectionAttrMethod(TypedName<?> n) {
            super(n);
        }

        @Override
        public void set(AbstractBeanEntityBuilder receiver, Object value) {
            try {
                setter.invoke(receiver, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("cannot invoke " + setter, e);
            }
        }

        @Override
        public void clear(AbstractBeanEntityBuilder receiver) {
            try {
                if (clearer != null) {
                    clearer.invoke(receiver);
                } else {
                    setter.invoke(receiver, null);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("cannot invoke " + (clearer != null ? clearer : setter), e);
            }
        }
    }
}
