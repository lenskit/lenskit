package org.grouplens.lenskit.eval.config;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

/**
 * An eval "project", the eval script equivalent of an Ant project.  This is used and configured
 * by an {@link EvalScript} and provides access to options, targets, etc.
 *
 * @since 1.3
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class EvalProject {
    private Project antProject;
    private Random random = new Random();

    /**
     * Construct a new eval project.
     * @param props A set of additional properties.  These properties will be available in the
     *              project, in addition to the system properties.  This is not where properties
     *              from the command line should be supplied; those should be set with
     *              {@link #setUserProperty(String, String)} so that they have Ant-like behavior.
     */
    public EvalProject(@Nullable Properties props) {
        antProject = new Project();
        antProject.init();
        if (props != null) {
            PropertyHelper ph = PropertyHelper.getPropertyHelper(antProject);
            for (Map.Entry<Object,Object> prop: props.entrySet()) {
                ph.setProperty(prop.getKey().toString(), prop.getValue().toString(), false);
            }
        }
    }

    /**
     * Get the Ant project from this eval project.
     *
     * @return The Ant project.
     */
    public Project getAntProject() {
        return antProject;
    }

    /**
     * Get the project's random number generator.
     * @return The project's random number generator.
     */
    public Random getRandom() {
        return random;
    }

    /**
     * Set the project's random number generator.
     * @param random The project's random number generator.
     */
    public void setRandom(Random random) {
        this.random = random;
    }

    /**
     * Get the (legacy) configuration view of this project.
     * @return An accessor for the project's configuration.
     */
    public EvalConfig getConfig() {
        return new EvalConfig(antProject.getProperties());
    }

    /**
     * Get a property value.
     * @param propertyName The property name.
     * @return The property value, or {@code null} if no such property exists.
     */
    public String getProperty(String propertyName) {
        return antProject.getProperty(propertyName);
    }

    /**
     * Set a "user" property.  These properties cannot be overridden by property sets in the script.
     * @param name The property to set.
     * @param value The property value.
     */
    public void setUserProperty(String name, String value) {
        antProject.setUserProperty(name, value);
    }

    /**
     * Execute a target in the project.
     * @param name The target to execute.
     */
    public void executeTarget(String name) throws BuildException {
        antProject.executeTarget(name);
    }
}
