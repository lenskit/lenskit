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

import com.google.common.base.Preconditions;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.lang.GroovyRuntimeException;
import org.lenskit.LenskitConfiguration;
import org.lenskit.RecommenderConfigurationException;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * LensKit configuration helper utilities.
 *
 * @since 1.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ConfigHelpers {
    private ConfigHelpers() {
    }

    /**
     * Load a LensKit configuration from a Groovy closure.  This is useful for using the Groovy
     * DSL in unit tests.
     *
     * @param block The block to evaluate.  This block will be evaluated with a delegate providing
     *              the LensKit DSL and the {@link groovy.lang.Closure#DELEGATE_FIRST} resolution strategy.
     * @return The LensKit configuration.
     * @see ConfigurationLoader#load(groovy.lang.Closure)
     */
    public static LenskitConfiguration load(@DelegatesTo(LenskitConfigDSL.class) Closure<?> block) throws RecommenderConfigurationException {
        Preconditions.checkNotNull(block, "Configuration block");
        LenskitConfiguration config = new LenskitConfiguration();
        configure(config, block);
        return config;
    }

    /**
     * Load a LensKit configuration from a script (as a string).
     *
     * @param script The script source text to evaluate.
     * @return The LensKit configuration.
     * @deprecated Loading from Groovy source strings is confusing.
     */
    @Deprecated
    public static LenskitConfiguration load(String script) throws RecommenderConfigurationException {
        return new ConfigurationLoader().load(script);
    }

    /**
     * Load a LensKit configuration from a script file.
     *
     * @param script The script source file to evaluate.
     * @return The LensKit configuration.
     */
    public static LenskitConfiguration load(File script) throws IOException, RecommenderConfigurationException {
        return new ConfigurationLoader().load(script);
    }

    /**
     * Load a LensKit configuration from a script URL.
     *
     * @param script The script source URL to evaluate.
     * @return The LensKit configuration.
     */
    public static LenskitConfiguration load(URL script) throws IOException, RecommenderConfigurationException {
        return new ConfigurationLoader().load(script);
    }

    /**
     * Modify a configuration from a closure. The class loader is not really consulted in this case.
     * @param block The block to evaluate. This block will be evaluated with a delegate providing
     *              the LensKit DSL and the {@link Closure#DELEGATE_FIRST} resolution strategy.
     */
    public static void configure(LenskitConfiguration config,
                                 @Nonnull @DelegatesTo(LenskitConfigDSL.class) Closure<?> block) throws RecommenderConfigurationException {
        Preconditions.checkNotNull(block, "Configuration block");
        BindingDSL delegate = LenskitConfigDSL.forConfig(config);
        try {
            GroovyUtils.callWithDelegate(block, delegate);
        } catch (GroovyRuntimeException e) {
            // this quite possibly wraps an exception we want to throw
            if (e.getClass().equals(GroovyRuntimeException.class) && e.getCause() != null) {
                throw new RecommenderConfigurationException("Error evaluating Groovy block",
                                                            e.getCause());
            } else {
                throw new RecommenderConfigurationException("Error evaluating Groovy block", e);
            }
        } catch (RuntimeException e) {
            throw new RecommenderConfigurationException("Error evaluating Groovy block", e);
        }
    }
}
