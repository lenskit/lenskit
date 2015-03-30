/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.grouplens.lenskit.gradle

import com.google.common.collect.FluentIterable
import org.gradle.api.Nullable
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.ConventionTask
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.JavaExecSpec
import org.gradle.process.internal.DefaultJavaExecAction
import org.gradle.util.ConfigureUtil

/**
 * Task to run LensKit evaluations.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LenskitEval extends ConventionTask {
    private Object script = "eval.groovy"
    private List<String> targets = new ArrayList<String>()
    private Map<String,Object> lkProps = new HashMap<String, Object>()
    def int threadCount
    def String maxMemory
    final def JavaExecSpec invoker

    public LenskitEval() {
        invoker = new DefaultJavaExecAction(services.get(FileResolver))
        invoker.classpath = project.configurations.getByName('lenskit')
    }

    /**
     * Set the evaluation script to run.
     * @param obj The script to run (interpreted by {@link Project#file(Object)}).
     */
    public void script(Object obj) {
        script = obj;
    }

    /**
     * Set the script to be run.
     * @param obj The script to run (interpreted by {@link Project#file(Object)}).
     */
    public void setScript(Object obj) {
        script = obj;
    }

    /**
     * Get the script to be run.
     * @return The script to be run.
     */
    public Object getScript() {
        return script;
    }

    /**
     * Specify targets to run.
     * @param tgts Some targets to run.
     */
    public void targets(String... tgts) {
        for (String t: tgts) {
            targets.add(t);
        }
    }

    /**
     * Set the list of targets to run.
     * @param tgts The list of targets to run.
     */
    public void setTargets(Iterable<String> tgts) {
        targets = FluentIterable.from(tgts).toList();
    }

    /**
     * Get the targets to be run.
     * @return The list of targets to be run.
     */
    public List<String> getTargets() {
        return targets;
    }

    /**
     * Get the map of properties to be passed to the script.
     * @return The map of the properties to be passed to the script.
     */
    public Map<String, Object> getLenskitProperties() {
        return lkProps;
    }

    /**
     * Set the map of properties to be passed to the script.
     * @param props The map of properties to be passed.
     */
    public void setLenskitProperties(Map<String, Object> props) {
        lkProps = new HashMap<String, Object>(props);
    }

    /**
     * Set some properties to be passed to the script.
     * @param props A map of properties to set.  These properties are set in addition to any
     *              already-set properties, overriding previously-set properties with the same value.
     */
    public void lenskitProperties(Map<String, Object> props) {
        lkProps.putAll(props);
    }

    @Nullable
    private LenskitExtension getLenskitConfig() {
        return (LenskitExtension) getProject().getExtensions().findByName("lenskit");
    }

    public void threadCount(int tc) {
        setThreadCount(tc);
    }

    public FileCollection getClasspath() {
        return invoker.classpath
    }

    public void setClasspath(FileCollection cp) {
        invoker.classpath = cp
    }

    public void classpath(FileCollection cp) {
        invoker.classpath = cp
    }

    public void maxMemory(String mm) {
        maxMemory = mm
    }

    public void invoker(Closure block) {
        ConfigureUtil.configure(block, invoker)
    }

    /**
     * Get the script file to run.
     *
     * @return The name of the script file to run.
     */
    @InputFile
    public File getScriptFile() {
        return getProject().file(script);
    }

    @TaskAction
    public void exec() {
        logger.info 'Running evaluation {}', scriptFile
        // FIXME It isn't obvious why the thread count has to be this way
        // It has to do with convention mappings, but we need to be clearer
        logger.info 'Thread count: {}', getThreadCount()
        logger.info 'Max memory: {}', maxMemory
        // grab reference to make scope clearer
        def lke = this
        invoker {
            main = 'org.lenskit.cli.Main'
            args 'eval'
            args "-j$lke.threadCount"
            for (prop in lke.lenskitProperties) {
                args "-D$prop.key=$prop.value"
            }
            if (lke.scriptFile != null) {
                args '-f', lke.scriptFile
            }
            args lke.targets
            if (lke.maxMemory != null) {
                maxHeapSize = lke.maxMemory
            }
        }
        invoker.execute()
    }
}
