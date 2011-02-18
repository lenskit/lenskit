package org.grouplens.reflens.params.meta;

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
