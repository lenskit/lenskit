package org.grouplens.lenskit.core;

import com.google.common.collect.Sets;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * Annotation processor to provide basic linting of LensKit {@code Shareable} annotations.
 *
 * @see {@link Shareable}
 */
@SupportedAnnotationTypes("org.grouplens.lenskit.core.Shareable")
public class ShareableAnnotationProcessor extends AbstractProcessor {
    public ShareableAnnotationProcessor() {}

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        note("LensKit Shareable linting active");
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
        //return; // do nothing to avoid being noisy
        String msg = String.format(fmt, args);
        getLog().printMessage(Diagnostic.Kind.NOTE, msg);
    }

    private void warning(String fmt, Object... args) {
        String msg = String.format(fmt, args);
        getLog().printMessage(Diagnostic.Kind.WARNING, msg);
    }

    private void error(String fmt, Object... args) {
        String msg = String.format(fmt, args);
        getLog().printMessage(Diagnostic.Kind.ERROR, msg);
    }

    private void mandatoryWarning(Element e, String fmt, Object... args) {
        String msg = String.format(fmt, args);
        if (e == null) {
            getLog().printMessage(Diagnostic.Kind.MANDATORY_WARNING, msg);
        } else {
            getLog().printMessage(Diagnostic.Kind.MANDATORY_WARNING, msg, e);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elts = roundEnv.getElementsAnnotatedWith(Shareable.class);
        note("processing %d shareable elements", elts.size());
        Types types = processingEnv.getTypeUtils();
        Elements elements = processingEnv.getElementUtils();
        TypeMirror serializable = elements.getTypeElement("java.io.Serializable").asType();
        for (Element elt: elts) {
            note("examining %s", elt);
            TypeMirror type = elt.asType();
            if (types.isAssignable(type, serializable)) {
                note("shareable type %s is serializable", type);
            } else {
                mandatoryWarning(elt, "shareable type %s is not serializable", type);
            }
        }
        return true;
    }
}
