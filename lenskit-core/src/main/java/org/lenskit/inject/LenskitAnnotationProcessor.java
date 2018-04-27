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
package org.lenskit.inject;

import org.grouplens.grapht.annotation.AliasFor;
import org.lenskit.inject.Parameter;
import org.lenskit.inject.Shareable;

import javax.annotation.processing.*;
import javax.inject.Qualifier;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * Annotation processor to provide basic linting of LensKit annotations.
 *
 * @see Shareable
 * @see Parameter
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
        // we can't compile against RELEASE_7 and maintain Java 6 compatibility, but the
        // processor is Java 7-compatible. We have not tested against Java 8, however.
        SourceVersion[] versions = SourceVersion.values();
        SourceVersion v6 = SourceVersion.RELEASE_6;
        assert v6.ordinal() < versions.length;
        // we support up through Java 8
        return versions[Math.min(v6.ordinal() + 2, versions.length - 1)];
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
            if (elt instanceof TypeElement) {
                TypeMirror type = elt.asType();
                if (typeUtils.isAssignable(type, serializable)) {
                    note("shareable type %s is serializable", type);
                } else {
                    warning(elt, "shareable type %s is not serializable", type);
                }
            } else {
                note("non-type element %s cannot be verified serializable", elt);
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
            AliasFor alias = param.getAnnotation(AliasFor.class);
            if (alias != null) {
                warning(param, "parameter %s is an alias (@Parameter should be on target)", param);
            }
        }
    }
}
