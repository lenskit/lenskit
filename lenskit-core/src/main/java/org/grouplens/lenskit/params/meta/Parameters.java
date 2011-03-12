/*
 * RefLens, a reference implementation of recommender algorithms.
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
package org.grouplens.lenskit.params.meta;

import java.lang.annotation.Annotation;

/**
 * Utilities for inspecting and manipulating parameters.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public final class Parameters {
	public static boolean hasDefaultInt(Class<? extends Annotation> annot) {
		return annot.isAnnotationPresent(DefaultInt.class);
	}
	public static int getDefaultInt(Class<? extends Annotation> annot) {
		DefaultInt dft = annot.getAnnotation(DefaultInt.class);
		return dft.value();
	}
	public static boolean hasDefaultDouble(Class<? extends Annotation> annot) {
		return annot.isAnnotationPresent(DefaultDouble.class);
	}
	public static double getDefaultDouble(Class<? extends Annotation> annot) {
		DefaultDouble dft = annot.getAnnotation(DefaultDouble.class);
		return dft.value();
	}
	public static boolean hasDefaultClass(Class<? extends Annotation> annot) {
		return annot.isAnnotationPresent(DefaultClass.class);
	}
	public static Class<?> getDefaultClass(Class<? extends Annotation> annot) {
		DefaultClass dft = annot.getAnnotation(DefaultClass.class);
		return dft.value();
	}
}
