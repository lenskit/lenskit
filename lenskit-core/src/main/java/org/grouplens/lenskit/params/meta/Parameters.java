package org.grouplens.lenskit.params.meta;

import java.lang.annotation.Annotation;

public class Parameters {
    public static Integer getDefaultInt(Class<? extends Annotation> annot) {
        DefaultInt dft = annot.getAnnotation(DefaultInt.class);
        return (dft == null ? null : dft.value());
    }
    
    public static Double getDefaultDouble(Class<? extends Annotation> annot) {
        DefaultDouble dft = annot.getAnnotation(DefaultDouble.class);
        return (dft == null ? null : dft.value());
    }
    
    public static Class<?> getDefaultClass(Class<? extends Annotation> annot) {
        DefaultClass dft = annot.getAnnotation(DefaultClass.class);
        return (dft == null ? null : dft.value());
    }
    
    public static Class<?> getParameterType(Class<? extends Annotation> annot) {
        Parameter param = annot.getAnnotation(Parameter.class);
        return (param == null ? null : param.value());
    }
    
    public static boolean isParameter(Class<? extends Annotation> annot) {
        return annot.getAnnotation(Parameter.class) != null;
    }
}
