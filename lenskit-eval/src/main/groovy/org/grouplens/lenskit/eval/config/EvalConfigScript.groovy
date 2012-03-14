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

import org.slf4j.LoggerFactory

/**
 * Base class for evaluator configuration scripts. It contains the metaclass
 * machinery to set up evaluation tasks.
 * @author Michael Ekstrand
 * @since 0.10
 */
abstract class EvalConfigScript extends Script {
    protected final def logger = LoggerFactory.getLogger(getClass())
    private EvalConfigEngine engine

    EvalConfigScript() {
        engine = null
    }

    EvalConfigScript(EvalConfigEngine eng) {
        engine = eng
    }

    void setEngine(EvalConfigEngine eng) {
        engine = eng
    }

    EvalConfigEngine getEngine() {
        return engine
    }

    def methodMissing(String name, args) {
        logger.debug("searching for eval task {}", name)
        def method = ConfigHelpers.findBuilderMethod(engine, name, args)
        if (method != null) {
            def obj = method()
            return obj
        } else {
            throw new MissingMethodException(name, this.class, args)
        }
    }

    def run() {
        try {
            return super.run()
        } catch (MissingPropertyException mpe) {
            def msg = "Could not resolve property ${mpe.property}; maybe an import is missing?"
            throw new RuntimeException(msg, mpe);
        }
    }
}
