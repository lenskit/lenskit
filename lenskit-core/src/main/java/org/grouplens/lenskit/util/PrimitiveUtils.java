/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.util;

public class PrimitiveUtils {

    public static boolean isBoxedTypePrimitive(Class<?> type) {
        type = box(type);
        return Number.class.isAssignableFrom(type) || 
               Boolean.class.isAssignableFrom(type) || 
               Character.class.isAssignableFrom(type);
    }
    
    public static Class<?> box(Class<?> type) {
        if (type.isPrimitive()) {
            if (type.equals(int.class))
                return Integer.class;
            else if (type.equals(boolean.class))
                return Boolean.class;
            else if (type.equals(long.class))
                return Long.class;
            else if (type.equals(float.class))
                return Float.class;
            else if (type.equals(double.class))
                return Double.class;
            else if (type.equals(char.class))
                return Character.class;
            else if (type.equals(byte.class))
                return Byte.class;
            else if (type.equals(short.class))
                return Short.class;
        }
        return type;
    }
    
    public static Class<?> unboox(Class<?> type) {
        if (type.equals(Integer.class))
            return int.class;
        else if (type.equals(Boolean.class))
            return boolean.class;
        else if (type.equals(Long.class))
            return long.class;
        else if (type.equals(Float.class))
            return float.class;
        else if (type.equals(Double.class))
            return double.class;
        else if (type.equals(Character.class))
            return char.class;
        else if (type.equals(Byte.class))
            return byte.class;
        else if (type.equals(Short.class))
            return short.class;
        else
            return type;
    }
    
    public static Number cast(Class<? extends Number> newType, Number value) {
        if (Integer.class.equals(newType))
            return value.intValue();
        else if (Short.class.equals(newType))
            return value.shortValue();
        else if (Long.class.equals(newType))
            return value.longValue();
        else if (Double.class.equals(newType))
            return value.doubleValue();
        else if (Float.class.equals(newType))
            return value.floatValue();
        else if (Byte.class.equals(newType))
            return value.byteValue();
        else
            throw new UnsupportedOperationException("Unknown number type: " + newType);
    }
}
