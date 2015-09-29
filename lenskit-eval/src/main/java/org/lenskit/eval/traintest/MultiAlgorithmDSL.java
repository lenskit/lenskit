package org.lenskit.eval.traintest;

import com.google.common.collect.ImmutableList;
import groovy.lang.Closure;
import org.grouplens.lenskit.config.ConfigurationLoader;
import org.grouplens.lenskit.config.LenskitConfigDSL;

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
        super(loader, aib.getConfig());
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
        MultiAlgorithmDSL dsl = new MultiAlgorithmDSL(getConfigLoader(), builder);
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
