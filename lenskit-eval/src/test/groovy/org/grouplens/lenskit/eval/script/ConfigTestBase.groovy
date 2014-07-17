/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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
package org.grouplens.lenskit.eval.script

import org.junit.Before

/**
 * Base/helper class for testing configuration code snippets. Provides an
 * method which runs a code snippet as if it were a config script and returns the result.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
abstract class ConfigTestBase {
    protected EvalScriptEngine engine

    @Before
    public void createEngine() {
        engine = new EvalScriptEngine()
    }

    /**
     * Evalate a closure as if it were a config snippet.
     * @param cl The code to run.
     * @return The return value of evaluating {@code cl}.
     */
    protected def eval(@DelegatesTo(EvalScript) Closure cl) {
        def script = new ClosureScript(engine, cl)
        return engine.runScript(script, engine.createProject())
    }

    protected EvalScript evalScript(@DelegatesTo(EvalScript) Closure cl) {
        def script = new ClosureScript(engine, cl);
        engine.runScript(script, engine.createProject())
        script
    }
}
