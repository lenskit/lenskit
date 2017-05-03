/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.config;

import com.google.common.base.Joiner;
import groovy.lang.Binding;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.lenskit.LenskitConfiguration;
import org.lenskit.RecommenderConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
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
    private Map<String,Set<String>> badProperties = new LinkedHashMap<>();

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
            logger.error("unresolved class or property {}", name);
            Set<String> packages = delegate.getConfigLoader().getDirectory().getPackages(name);
            logger.debug("found {} packages with classes named {}", packages.size(), name);
            badProperties.put(name, packages);
            return null;
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
        badProperties.clear();
        try {
            run();
        } catch (RecommenderConfigurationException rce) {
            throw rce;
        } catch (Exception ex) {
            throw new RecommenderConfigurationException("error configuring recommender", ex);
        }
        if (!badProperties.isEmpty()) {
            String message = "Unresolved properties in evaluation script: ";
            message += Joiner.on(", ").join(badProperties.keySet());
            RecommenderConfigurationException ex = new RecommenderConfigurationException(message);
            for (Map.Entry<String,Set<String>> bpe: badProperties.entrySet()) {
                logger.error("Script references unknown class or property {}", bpe.getKey());
                for (String pkg: bpe.getValue()) {
                    logger.info("consider importing {}.{}", pkg, bpe.getKey());
                    ex.addHint("consider importing %s.%s", pkg, bpe.getKey());
                }
            }
            throw ex;
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
