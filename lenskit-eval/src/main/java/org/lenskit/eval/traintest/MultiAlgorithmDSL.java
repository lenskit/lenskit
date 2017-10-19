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
package org.lenskit.eval.traintest;

import com.google.common.collect.ImmutableList;
import groovy.lang.Closure;
import org.lenskit.config.ConfigurationLoader;
import org.lenskit.config.LenskitConfigDSL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DSL delegate for configuring multiple LensKit algorithms with names and attributes.  It adds two new capabilities:
 *
 * -   The {@link #getAttributes()} property exposes the algorithm instance attributes for manipulation.
 * -   The {@link #algorithm(String, Closure)} method allows for `algorithm` blocks so more than one algorithm can be
 *     defined in a script.
 *
 * If `algorithm` blocks are used in a script, then only those blocks' algorithms are generated; a top-level algorithm
 * is **not** generated.  `algorithm` blocks also nest, and only those blocks that have no child `algorithm` blocks
 * actually produce new algorithms.
 */
public class MultiAlgorithmDSL extends LenskitConfigDSL {
    private final AlgorithmInstanceBuilder builder;
    private List<AlgorithmInstance> instances = new ArrayList<>();

    public MultiAlgorithmDSL(ConfigurationLoader loader, AlgorithmInstanceBuilder aib) {
        super(loader, aib.getConfig(), null);
        builder = aib;
    }

    public List<AlgorithmInstance> getInstances() {
        return ImmutableList.copyOf(instances);
    }

    public Map<String,Object> getAttributes() {
        return builder.getAttributes();
    }

    public void algorithm(Closure<?> block) {
        algorithm(null, block);
    }

    /**
     * Create a new algorithm in this file.
     *
     * @param name The algorithm name.
     * @param block The configuration block.
     */
    public void algorithm(String name, Closure<?> block) {
        AlgorithmInstanceBuilder kid = builder.extend();
        if (name != null) {
            kid.setName(name);
        }
        MultiAlgorithmDSL dsl = new MultiAlgorithmDSL(getConfigLoader(), kid);
        dsl.setBaseURI(getBaseURI());
        Closure<?> copy = (Closure<?>) block.clone();
        copy.setDelegate(dsl);
        copy.setResolveStrategy(Closure.DELEGATE_FIRST);
        copy.call();
        List<AlgorithmInstance> kidInstances = dsl.getInstances();
        if (kidInstances.isEmpty()) {
            instances.add(kid.build());
        } else {
            instances.addAll(kidInstances);
        }
    }
}
