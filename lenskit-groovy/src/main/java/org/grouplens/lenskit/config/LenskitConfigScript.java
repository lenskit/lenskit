package org.grouplens.lenskit.config;

import groovy.lang.Binding;
import groovy.lang.Script;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

/**
 * Base class for LensKit configuration scripts.  This class mixes in {@code LenskitConfigDSL}, so
 * all methods on that class are directly available in configuraiton scripts.
 *
 * @since 1.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class LenskitConfigScript extends Script {
    static {
        DefaultGroovyMethods.mixin(LenskitConfigScript.class, LenskitConfigDSL.class);
    }
    protected LenskitConfigScript() {
    }

    protected LenskitConfigScript(Binding binding) {
        super(binding);
    }
}
