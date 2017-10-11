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
package org.lenskit.config;

import groovy.lang.Binding;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.lenskit.LenskitConfiguration;
import org.lenskit.RecommenderConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Base class for LensKit configuration scripts.  This class mixes in {@code LenskitConfigDSL}, so
 * all methods on that class are directly available in configuraiton scripts.
 *
 * @since 1.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class LenskitConfigScript extends Script {
    protected final Logger logger = LoggerFactory.getLogger(LenskitConfigScript.class);
    private LenskitConfigDSL delegate;

    protected LenskitConfigScript() {
        this(new Binding());
    }

    protected LenskitConfigScript(Binding binding) {
        super(binding);
    }

    public LenskitConfiguration getConfig() {
        return delegate.getConfig();
    }

    /**
     * Get the delegate.
     * @return The DSL delegate.
     */
    public LenskitConfigDSL getDelegate() {
        if (delegate == null) {
            throw new IllegalStateException("no delegate set");
        }
        return delegate;
    }

    /**
     * Set the delegate.
     * @param dsl The delegate.
     */
    public void setDelegate(LenskitConfigDSL dsl) {
        delegate = dsl;
    }

    /**
     * Groovy override to pass things off to the delegate.
     * @param name The name of the method.
     * @param args The method arguments.
     * @return The return value of the method.
     */
    public Object methodMissing(String name, Object args) {
        try {
            return InvokerHelper.invokeMethod(getDelegate(), name, args);
        } catch (MissingMethodException mme) {
            throw new MissingMethodException(name, getClass(), mme.getArguments());
        }
    }

    /**
     * Groovy override to provide usage hints with missing properties.
     * @param name The name of the missing property
     * @return The (simulated) property value.
     */
    public Object propertyMissing(String name) {
        if (Character.isUpperCase(name.charAt(0))) {
            logger.error("unresolved class or property {}, missing import?", name);
            Set<String> packages = delegate.getConfigLoader().getDirectory().getPackages(name);
            logger.debug("found {} packages with classes named {}", packages.size(), name);
            if (packages.isEmpty()) {
                throw new MissingPropertyException(name, getClass());
            }

            String message = "Unresolved property in evaluation script: " + name;
            RecommenderConfigurationException ex = new RecommenderConfigurationException(message);
            for (String pkg: packages) {
                logger.info("consider importing {}.{}", pkg, name);
                ex.addHint("consider importing %s.%s", pkg, name);
            }
            throw ex;
        } else {
            logger.error("unresolved property {} in configuration script", name);
            throw new MissingPropertyException(name, getClass());
        }
    }

    /**
     * Run the script and check for error conditions.
     * @throws RecommenderConfigurationException if there is an error with the configuration.
     */
    private void runScript() throws RecommenderConfigurationException {
        try {
            run();
        } catch (RecommenderConfigurationException rce) {
            throw rce;
        } catch (Exception ex) {
            throw new RecommenderConfigurationException("error configuring recommender", ex);
        }
    }

    /**
     * Run this script against an existing configuration.
     * @throws RecommenderConfigurationException if an error occurs.
     */
    public void configure(LenskitConfiguration config) throws RecommenderConfigurationException {
        LenskitConfigDSL old = getDelegate();
        setDelegate(new LenskitConfigDSL(old.getConfigLoader(), config, delegate.getBaseURI()));
        try {
            runScript();
        } finally {
            setDelegate(old);
        }
    }

    /**
     * Run this script and produce a new configuration.
     * @return The configuration.
     * @throws RecommenderConfigurationException if an error occurs.
     */
    public LenskitConfiguration configure() throws RecommenderConfigurationException {
        runScript();
        return delegate.getConfig();
    }
}
