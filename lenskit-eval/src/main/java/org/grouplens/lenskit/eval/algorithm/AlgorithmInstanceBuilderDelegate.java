package org.grouplens.lenskit.eval.algorithm;

import org.grouplens.lenskit.config.LenskitConfigDSL;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;

import java.util.Map;

/**
 * Groovy delegate for configuring {@code AlgorithmInstanceCommand}s.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.10
 */
public class AlgorithmInstanceBuilderDelegate extends LenskitConfigDSL {
    private LenskitAlgorithmInstanceBuilder builder;

    public AlgorithmInstanceBuilderDelegate(LenskitAlgorithmInstanceBuilder builder) {
        super(builder.getConfig());
        this.builder = builder;
    }

    public LenskitRecommenderEngineFactory getFactory() {
        return builder.getFactory();
    }

    public Map<String, Object> getAttributes() {
        return builder.getAttributes();
    }

    public boolean getPreload() {
        return builder.getPreload();
    }

    public void setPreload(boolean pl) {
        builder.setPreload(pl);
    }

    public String getName() {
        return builder.getName();
    }

    public void setName(String name) {
        builder.setName(name);
    }
}
