/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

    def getAttributes() {
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

    def methodMissing(String name, args) {
        if (name in ["set", "setBuilder", "setComponent"]) {
            factory.metaClass.invokeMethod(factory, name, args)
        } else {
            null
        }
    }
}
