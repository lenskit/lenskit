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
