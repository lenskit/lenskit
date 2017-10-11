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
package org.lenskit.util.reflect;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Type.getInternalName;

/**
 * Code generation utilities.
 */
public class CGUtils {
    /**
     * Emit instructions that adapt the current stack (an Object) to a possibly primitive target type.
     * @param method The method to which code should be added.
     * @param type The target type.
     * @return The number of additional stack elements needed (usually 1 or 0).
     */
    public static int adaptToType(MethodVisitor method, Class<?> type) {
        if (type.isPrimitive()) {
            if (type.equals(long.class)) {
                method.visitTypeInsn(CHECKCAST, getInternalName(Long.class));
                method.visitMethodInsn(INVOKEVIRTUAL, getInternalName(Long.class),
                                       "longValue", "()J", false);
                return 1;
            } else if (type.equals(int.class)) {
                method.visitTypeInsn(CHECKCAST, getInternalName(Integer.class));
                method.visitMethodInsn(INVOKEVIRTUAL, getInternalName(Integer.class),
                                       "intValue", "()I", false);
                return 0;
            } else if (type.equals(double.class)) {
                method.visitTypeInsn(CHECKCAST, getInternalName(Double.class));
                method.visitMethodInsn(INVOKEVIRTUAL, getInternalName(Double.class),
                                       "doubleValue", "()D", false);
                return 1;
            } else {
                throw new IllegalArgumentException("type " + type + " not yet supported");
            }
        } else {
            method.visitTypeInsn(CHECKCAST, getInternalName(type));
            return 0;
        }
    }

    /**
     * Emit instructions that adapt the current stack contents to `Object`.
     * @param method The method.
     * @param type The type of the current stack contents, possibly primitive.
     */
    public static void adaptFromType(MethodVisitor method, Class<?> type) {
        if (type.isPrimitive()) {
            if (type.equals(long.class)) {
                method.visitMethodInsn(INVOKESTATIC, "java/lang/Long",
                                       "valueOf", "(J)Ljava/lang/Long;", false);
            } else if (type.equals(int.class)) {
                method.visitMethodInsn(INVOKESTATIC, "java/lang/Integer",
                                       "valueOf", "(I)Ljava/lang/Integer;", false);
            } else if (type.equals(double.class)) {
                method.visitMethodInsn(INVOKESTATIC, "java/lang/Double",
                                       "valueOf", "(D)Ljava/lang/Double;", false);
            } else {
                throw new IllegalArgumentException("type " + type + " not yet supported");
            }
        } else {
            /* object type is object */
        }
    }
}
