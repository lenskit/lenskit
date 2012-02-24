package org.grouplens.lenskit.eval.config

import org.grouplens.lenskit.eval.AlgorithmBuilder
import org.codehaus.groovy.runtime.MetaClassHelper
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory

/**
 * Groovy delegate for configuring {@code AlgorithmBuilder}s.
 * @author Michael Ekstrand
 * @since 0.10
 */
class AlgorithmBuilderDelegate {
    private AlgorithmBuilder builder

    AlgorithmBuilderDelegate(AlgorithmBuilder builder) {
        this.builder = builder
    }

    LenskitRecommenderEngineFactory getFactory() {
        return builder.getFactory()
    }

    Map<String,Object> getAttributes() {
        return builder.attributes
    }

    boolean getPreload() {
        return builder.getPreload()
    }

    void setPreload(boolean pl) {
        builder.setPreload(pl)
    }

    String getName() {
        return builder.getName()
    }

    void setName(String name) {
        builder.setName(name)
    }

    void propertyMissing(String name, value) {
        builder.setAttribute(name, value)
    }

    def methodMissing(String name, args) {
        if (name in ["set", "setBuilder", "setComponent"]) {
            factory.metaClass.invokeMethod(factory, name, args)
        } else {
            null
        }
    }
}
