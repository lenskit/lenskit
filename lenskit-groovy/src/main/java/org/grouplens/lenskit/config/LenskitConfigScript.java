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
