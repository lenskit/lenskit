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
import groovy.lang.Script;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.grouplens.lenskit.core.LenskitConfiguration;

/**
 * Base class for LensKit configuration scripts.  This class mixes in {@code LenskitConfigDSL}, so
 * all methods on that class are directly available in configuraiton scripts.
 *
 * @since 1.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class LenskitConfigScript extends Script {
    /*
     * This class exists to be a base class for LensKit configuration scripts.  Java does not have
     * multiple inheritance, but Groovy has mixins via its meta object protocol.  We mix the
     * LensKit config DSL into this class; all the methods are available to scripts, and we can get
     * the configuration by asking the metaclass for the "config" property.  These extra methods
     * and properties won't be easily available from Java, but that's OK, this class is only ever
     * used as the base class for Groovy scripts.
     */
    static {
        DefaultGroovyMethods.mixin(LenskitConfigScript.class, LenskitConfigDSL.class);
    }
    protected LenskitConfigScript() {
    }

    protected LenskitConfigScript(Binding binding) {
        super(binding);
    }

    public LenskitConfiguration getConfig() {
        return (LenskitConfiguration) getMetaClass().getProperty(this, "context");
    }
}
