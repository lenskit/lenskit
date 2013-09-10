/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval;

import com.google.common.collect.Iterables;
import org.apache.tools.ant.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.util.*;

/**
 * An eval "project", the eval script equivalent of an Ant project.  This is used and configured
 * by an {@link org.grouplens.lenskit.eval.script.EvalScript} and provides access to options, targets, etc.
 *
 * @since 1.3
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class EvalProject {
    private static final Logger logger = LoggerFactory.getLogger(EvalProject.class);
    private Project antProject;
    private Random random = new Random();
    private String defaultTarget;

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
        antProject.addBuildListener(new Listener());
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

    /**
     * Execute a sequence of targets.
     * @param names The targets to execute.
     */
    @SuppressWarnings("rawtypes")
    public void executeTargets(String... names) {
        Vector targets = antProject.topoSort(names, antProject.getTargets(), false);
        antProject.executeSortedTargets(targets);
    }

    /**
     * Execute a sequence of targets.
     * @param names The targets to execute.
     */
    @SuppressWarnings("rawtypes")
    public void executeTargets(List<String> names) {
        String[] nameArray = Iterables.toArray(names, String.class);
        Vector targets = antProject.topoSort(nameArray, antProject.getTargets(), false);
        antProject.executeSortedTargets(targets);
    }

    public String getDefaultTarget() {
        return defaultTarget;
    }

    public void setDefaultTarget(String defaultTarget) {
        this.defaultTarget = defaultTarget;
    }

    private static class Listener implements BuildListener {
        String target;
        String task;

        @Override
        public void buildStarted(BuildEvent event) {
            logger.info("started build of project " + event.getProject());
        }

        @Override
        public void buildFinished(BuildEvent event) {
            logger.info("finished build of project " + event.getProject());
        }

        @Override
        public void targetStarted(BuildEvent event) {
            target = event.getTarget().getName();
            logger.info("running target " + target);
        }

        @Override
        public void targetFinished(BuildEvent event) {
            logger.debug("finished target {}", event.getTarget().getName());
            target = null;
        }

        @Override
        public void taskStarted(BuildEvent event) {
            task = event.getTask().getTaskName();
            logger.info("running task {}", task);
        }

        @Override
        public void taskFinished(BuildEvent event) {
            logger.debug("task {} finished", event.getTask().getTaskName());
            task = null;
        }

        @Override
        public void messageLogged(BuildEvent event) {
            switch (event.getPriority()) {
            case Project.MSG_ERR:
                logger.error("{}:{}: {}", target, task, event.getMessage());
                break;
            case Project.MSG_WARN:
                logger.warn("{}:{}: {}", target, task, event.getMessage());
                break;
            case Project.MSG_INFO:
                logger.info("{}:{}: {}", target, task, event.getMessage());
                break;
            case Project.MSG_DEBUG:
                logger.debug("{}:{}: {}", target, task, event.getMessage());
                break;
            case Project.MSG_VERBOSE:
                logger.trace("{}:{}: {}", target, task, event.getMessage());
                break;
            }
        }
    }
}
