/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.config;

import groovy.lang.Binding;
import groovy.lang.MissingMethodException;
import groovy.lang.Script;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.grouplens.lenskit.core.LenskitConfiguration;

/**
 * Base class for LensKit configuration scripts.  This class mixes in {@code LenskitConfigDSL}, so
 * all methods on that class are directly available in configuraiton scripts.
 *
 * @since 1.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class LenskitConfigScript extends Script {
    private LenskitConfigDSL delegate;

    protected LenskitConfigScript() {
        this(new Binding());
    }

    protected LenskitConfigScript(Binding binding) {
        super(binding);
        delegate = new LenskitConfigDSL();
    }

    void setConfig(LenskitConfiguration cfg) {
        delegate = new LenskitConfigDSL(cfg);
    }

    public LenskitConfiguration getConfig() {
        return delegate.getConfig();
    }

    /**
     * Groovy override to pass things off to the delegate.
     * @param name The name of the method.
     * @param args The method arguments.
     * @return The return value of the method.
     */
    public Object methodMissing(String name, Object args) {
        try {
            return InvokerHelper.invokeMethod(delegate, name, args);
        } catch (MissingMethodException mme) {
            throw new MissingMethodException(name, getClass(), mme.getArguments());
        }
    }
}
