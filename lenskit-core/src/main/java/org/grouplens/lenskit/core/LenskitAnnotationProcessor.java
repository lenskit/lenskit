package org.grouplens.lenskit.core;

import com.google.common.collect.Sets;

import javax.annotation.processing.*;
import javax.inject.Qualifier;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;

/**
 * Annotation processor to provide basic linting of LensKit annotations.
 *
 * @see {@link Shareable}
 * @see {@link Parameter}
 */
@SupportedAnnotationTypes("org.grouplens.lenskit.core.*")
public class LenskitAnnotationProcessor extends AbstractProcessor {
    public LenskitAnnotationProcessor() {}

    private Types typeUtils;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        note("LensKit Shareable linting active");
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        // support version 6 or 7
        // we can't compile against RELEASE_6 and maintain Java 7 compatibility, but the
        // processor is Java 7-compatible. We have not tested against Java 8, however.
        SourceVersion[] versions = SourceVersion.values();
        SourceVersion v6 = SourceVersion.RELEASE_6;
        if (versions.length > v6.ordinal() + 1) {
            // the runtime supports release 7, let's use it
            return versions[v6.ordinal() + 1];
        } else {
            return v6;
        }
    }

    private Messager getLog() {
        return processingEnv.getMessager();
    }

    private void note(String fmt, Object... args) {
        return; // do nothing to avoid being noisy
//        String msg = String.format(fmt, args);
//        getLog().printMessage(Diagnostic.Kind.NOTE, msg);
    }

    private void error(String fmt, Object... args) {
        String msg = String.format(fmt, args);
        getLog().printMessage(Diagnostic.Kind.ERROR, msg);
    }

    private void warning(Element e, String fmt, Object... args) {
        String msg = String.format(fmt, args);
        if (e == null) {
            getLog().printMessage(Diagnostic.Kind.MANDATORY_WARNING, msg);
        } else {
            getLog().printMessage(Diagnostic.Kind.MANDATORY_WARNING, msg, e);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        checkShareableComponents(roundEnv);
        checkParameters(roundEnv);
        return false;
    }

    /**
     * Check shareable components for serializability.
     * @param round The round environment.
     */
    private void checkShareableComponents(RoundEnvironment round) {
        Set<? extends Element> elts = round.getElementsAnnotatedWith(Shareable.class);
        note("processing %d shareable elements", elts.size());
        TypeMirror serializable = elementUtils.getTypeElement("java.io.Serializable").asType();
        for (Element elt: elts) {
            note("examining %s", elt);
            TypeMirror type = elt.asType();
            if (typeUtils.isAssignable(type, serializable)) {
                note("shareable type %s is serializable", type);
            } else {
                warning(elt, "shareable type %s is not serializable", type);
            }
        }
    }

    /**
     * Check parameter annotations for being qualifiers.
     * @param round The round environment.
     */
    private void checkParameters(RoundEnvironment round) {
        Set<? extends Element> params = round.getElementsAnnotatedWith(Parameter.class);
        note("processing %d parameter annotations", params.size());
        for (Element param: params) {
            Qualifier q = param.getAnnotation(Qualifier.class);
            if (q == null) {
                warning(param, "parameter %s is not annotated as a qualifier", param);
            }
            Retention rp = param.getAnnotation(Retention.class);
            if (rp == null || rp.value() != RetentionPolicy.RUNTIME) {
                warning(param, "parameter %s does not have runtime retention", param);
            }
        }
    }
}
