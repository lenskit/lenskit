package org.grouplens.lenskit.config;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.grouplens.lenskit.core.LenskitConfiguration;

import java.io.File;

/**
 * Load LensKit configurations using the configuration DSL.
 *
 * @since 1.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ConfigurationLoader {
    private final ClassLoader classLoader;
    private final GroovyShell shell;
    private final Binding binding;

    /**
     * Construct a new configuration loader. It uses the current thread's class loader.
     * @review Is this the classloader we should use?
     */
    public ConfigurationLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Construct a new configuration loader.
     * @param loader The class loader to use.
     */
    public ConfigurationLoader(ClassLoader loader) {
        classLoader = loader;
        binding = new Binding();
        CompilerConfiguration config = new CompilerConfiguration();
        config.setScriptBaseClass(LenskitConfigScript.class.getName());
        ImportCustomizer imports = new ImportCustomizer();
        imports.addStarImports("org.grouplens.lenskit");
        config.addCompilationCustomizers(imports);
        shell = new GroovyShell(loader, binding, config);
    }

    /**
     * Load a configuration from a file.
     * @param file The configuration script to load.
     * @return The resulting LensKit configuration.
     */
    public LenskitConfiguration load(File file) {
        return null;
    }

    /**
     * Load a configuration from a script source.
     * @param source The configuration script to load.
     * @return The resulting LensKit configuration.
     */
    public LenskitConfiguration load(String source) {
        Script script = shell.parse(source);
        script.run();
        return (LenskitConfiguration) script.getMetaClass().getProperty(script, "config");
    }

    /**
     * Load a configuration from a closure. The class loader is not really consulted in this case.
     * @param block The block to evaluate. This block will be evaluated with a delegate providing
     *              the LensKit DSL and the {@link Closure#DELEGATE_FIRST} resolution strategy.
     * @return The resulting LensKit configuration.
     */
    public LenskitConfiguration load(Closure<?> block) {
        LenskitConfiguration config = new LenskitConfiguration();
        BindingDSL delegate = new LenskitConfigDSL(config);
        block.setDelegate(delegate);
        block.setResolveStrategy(Closure.DELEGATE_FIRST);
        block.call();
        return config;
    }
}
