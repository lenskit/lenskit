/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.data.entities;

import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.lenskit.util.reflect.CGUtils;
import org.lenskit.util.reflect.DynamicClassLoader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

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
    public EntityBuilder setLongAttribute(TypedName<Long> name, long val) {
        AttrMethod e = findEntry(name);
        if (e instanceof LongAttrMethod) {
            ((LongAttrMethod) e).set(this, val);
        } else if (e != null) {
            e.set(this, val);
        } else {
            setExtraAttribute(name, val);
        }

        return this;
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
            Class smVtype = smethod.getParameterTypes()[0];
            Method cmethod = clearers.get(ce.getKey().getName());
            ClassNode cn = new ClassNode();
            cn.name = String.format("org/lenskit/generated/entities/AttrSet$$%s$$%s",
                                    type.getName().replace('.', '$'),
                                    ce.getValue().getName());
            cn.access = ACC_PUBLIC;
            cn.version = V1_8;
            Class<? extends AttrMethod> sc;
            if (attr.getRawType().equals(Long.class) && smVtype.equals(long.class)) {
                sc = LongAttrMethod.class;
            } else if (attr.getRawType().equals(Double.class) && smVtype.equals(double.class)) {
                sc = DoubleAttrMethod.class;
            } else {
                sc = AttrMethod.class;
            }
            cn.superName = getInternalName(sc);
            MethodNode ctor = generateBeanConstructor(sc);
            cn.methods.add(ctor);

            MethodNode setter = generateSetter(type, smethod);
            cn.methods.add(setter);
            if (attr.getRawType().equals(Long.class) && smVtype.equals(long.class)) {
                cn.methods.add(generateLongSetter(type, smethod));
            }
            if (attr.getRawType().equals(Double.class) && smVtype.equals(double.class)) {
                cn.methods.add(generateDoubleSetter(type, smethod));
            }

            MethodNode clearer = generateClearer(type, smethod, cmethod);
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

    private static MethodNode generateBeanConstructor(Class<? extends AttrMethod> superclass) {
        MethodNode ctor = new MethodNode();
        ctor.access = ACC_PUBLIC;
        ctor.desc = getMethodDescriptor(VOID_TYPE, getType(TypedName.class));
        ctor.name = "<init>";
        ctor.exceptions = Collections.emptyList();
        ctor.maxLocals = 2;
        ctor.maxStack = 2;
        // load instance ('this')
        ctor.visitVarInsn(ALOAD, 0);
        // load the attribute
        ctor.visitVarInsn(ALOAD, 1);
        // invoke superclass constructor with attribute
        ctor.visitMethodInsn(INVOKESPECIAL, getInternalName(superclass),
                             "<init>", ctor.desc, false);
        ctor.visitInsn(RETURN);
        return ctor;
    }

    private static MethodNode generateSetter(Class<? extends AbstractBeanEntityBuilder> type, Method smethod) {
        MethodNode setter = new MethodNode();
        setter.access = ACC_PUBLIC;
        setter.desc = getMethodDescriptor(VOID_TYPE,
                                          getType(AbstractBeanEntityBuilder.class),
                                          getType(Object.class));
        setter.name = "set";
        setter.exceptions = Collections.emptyList();
        setter.maxLocals = 3;
        setter.maxStack = 2;
        // load target object
        setter.visitVarInsn(ALOAD, 1);
        // cast target object
        setter.visitTypeInsn(CHECKCAST, getInternalName(type));
        // load attribute value
        setter.visitVarInsn(ALOAD, 2);
        // convert attribute value if necessary
        setter.maxStack += CGUtils.adaptToType(setter, smethod.getParameterTypes()[0]);
        // call real setter
        setter.visitMethodInsn(INVOKEVIRTUAL, getInternalName(type),
                               smethod.getName(), getMethodDescriptor(smethod),
                               false);
        setter.visitInsn(RETURN);
        return setter;
    }

    private static MethodNode generateLongSetter(Class<? extends AbstractBeanEntityBuilder> type, Method smethod) {
        MethodNode setter = new MethodNode();
        setter.access = ACC_PUBLIC;
        setter.desc = getMethodDescriptor(VOID_TYPE,
                                          getType(AbstractBeanEntityBuilder.class),
                                          getType(long.class));
        setter.name = "set";
        setter.exceptions = Collections.emptyList();
        setter.maxLocals = 4;
        setter.maxStack = 3;
        // load target object
        setter.visitVarInsn(ALOAD, 1);
        // cast target object
        setter.visitTypeInsn(CHECKCAST, getInternalName(type));
        // load attribute value
        setter.visitVarInsn(LLOAD, 2);
        // call real setter
        setter.visitMethodInsn(INVOKEVIRTUAL, getInternalName(type),
                               smethod.getName(), getMethodDescriptor(smethod),
                               false);
        setter.visitInsn(RETURN);
        return setter;
    }

    private static MethodNode generateDoubleSetter(Class<? extends AbstractBeanEntityBuilder> type, Method smethod) {
        MethodNode setter = new MethodNode();
        setter.access = ACC_PUBLIC;
        setter.desc = getMethodDescriptor(VOID_TYPE,
                                          getType(AbstractBeanEntityBuilder.class),
                                          getType(double.class));
        setter.name = "set";
        setter.exceptions = Collections.emptyList();
        setter.maxLocals = 4;
        setter.maxStack = 3;
        // load target object
        setter.visitVarInsn(ALOAD, 1);
        // cast target object
        setter.visitTypeInsn(CHECKCAST, getInternalName(type));
        // load attribute value
        setter.visitVarInsn(DLOAD, 2);
        // call real setter
        setter.visitMethodInsn(INVOKEVIRTUAL, getInternalName(type),
                               smethod.getName(), getMethodDescriptor(smethod),
                               false);
        setter.visitInsn(RETURN);
        return setter;
    }

    private static MethodNode generateClearer(Class<? extends AbstractBeanEntityBuilder> type, Method smethod, Method cmethod) {
        MethodNode clearer = new MethodNode();
        clearer.access = ACC_PUBLIC;
        clearer.desc = getMethodDescriptor(VOID_TYPE,
                                          getType(AbstractBeanEntityBuilder.class));
        clearer.name = "clear";
        clearer.exceptions = Collections.emptyList();
        clearer.maxLocals = 2;
        clearer.maxStack = 1;
        if (cmethod != null) {
            // load target object
            clearer.visitVarInsn(ALOAD, 1);
            // cast to target object type
            clearer.visitTypeInsn(CHECKCAST, getInternalName(type));
            // call clearer method
            clearer.visitMethodInsn(INVOKEVIRTUAL, getInternalName(type),
                                    cmethod.getName(), getMethodDescriptor(cmethod),
                                    false);
            clearer.visitInsn(RETURN);
        } else if (!smethod.getParameterTypes()[0].isPrimitive()) {
            // load target object & cast to type
            clearer.visitVarInsn(ALOAD, 1);
            clearer.visitTypeInsn(CHECKCAST, getInternalName(type));
            // load null and call setter
            clearer.visitInsn(ACONST_NULL);
            clearer.maxStack = 2;
            clearer.visitMethodInsn(INVOKEVIRTUAL, getInternalName(type),
                                    smethod.getName(), getMethodDescriptor(smethod),
                                    false);
            clearer.visitInsn(RETURN);
        } else {
            // no clearer and primitive method, throw unsupported operation exception
            clearer.maxStack = 2;
            clearer.visitTypeInsn(NEW, "java/lang/UnsupportedOperationException");
            clearer.visitInsn(DUP);
            clearer.visitMethodInsn(INVOKESPECIAL, "java/lang/UnsupportedOperationException", "<init>", "()V", false);
            clearer.visitInsn(ATHROW);
        }
        return clearer;
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

    public abstract static class LongAttrMethod extends AttrMethod {
        protected LongAttrMethod(TypedName<?> n) {
            super(n);
        }

        public abstract void set(AbstractBeanEntityBuilder receiver, long value);
    }

    public abstract static class DoubleAttrMethod extends AttrMethod {
        protected DoubleAttrMethod(TypedName<?> n) {
            super(n);
        }

        public abstract void set(AbstractBeanEntityBuilder receiver, double value);
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
