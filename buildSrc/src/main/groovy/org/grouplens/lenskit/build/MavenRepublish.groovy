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
package org.grouplens.lenskit.build

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier
import org.gradle.jvm.JvmLibrary
import org.gradle.language.base.artifact.SourcesArtifact
import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact

/**
 * Republish dependencies of a configuration to a Maven repository.
 */
public class MavenRepublish extends ConventionTask {
    public def Set<Configuration> configurations = []
    def repoRoot
    private def depFileCache = null

    public MavenRepublish() {
        conventionMapping.repoRoot = {
            "$project.buildDir/offline-repo"
        }
    }

    Multimap<ModuleComponentIdentifier,File> resolveDepFiles(cache = true) {
        if (cache && depFileCache != null) {
            return depFileCache
        }

        Multimap<ModuleComponentIdentifier,File> depFiles = HashMultimap.create()
        def ids = []
        for (cfg in configurations) {
            for (art in cfg.resolvedConfiguration.resolvedArtifacts) {
                def mid = art.moduleVersion.id
                if (mid.group == project.group) {
                    continue // skip artifacts from this project for now
                }
                def cid = new DefaultModuleComponentIdentifier(mid.group, mid.name, mid.version)
                logger.debug("found artifact for component {}", cid)
                ids.add(cid)
                depFiles.put(cid, art.file)
            }
        }

        // get source artifacts
        def res = project.dependencies.createArtifactResolutionQuery()
                         .forComponents(ids)
                         .withArtifacts(JvmLibrary, SourcesArtifact)
                         .execute()
        res.resolvedComponents.each { rcr ->
            def arts = rcr.getArtifacts(SourcesArtifact)
            depFiles.putAll(rcr.id, arts*.file)
        }

        // get Maven module artifacts
        def resolved = new HashSet()
        while (ids.any {!resolved.contains(it)}) {
            def mres = project.dependencies.createArtifactResolutionQuery()
                              .forComponents(ids - resolved)
                              .withArtifacts(MavenModule, MavenPomArtifact)
                              .execute()
            mres.resolvedComponents.each { rcr ->
                def arts = rcr.getArtifacts(MavenPomArtifact)
                logger.debug("handling Maven module for {} ({} artifacts)", rcr.id, arts.size())
                resolved.add(rcr.id)
                if (arts.isEmpty()) {
                    logger.warn("could not find Maven POM for {}", rcr.id)
                    return
                }
                def file = arts*.file.first()
                def parser = new MavenXpp3Reader()
                depFiles.put(rcr.id, file)
                def model = file.withReader {
                    parser.read(it)
                }
                if (model.parent != null) {
                    def pid = new DefaultModuleComponentIdentifier(model.parent.groupId,
                                                                   model.parent.artifactId,
                                                                   model.parent.version)
                    if (!resolved.contains(pid)) {
                        ids.add(pid)
                    }
                }
            }
        }

        depFileCache = depFiles
        return depFiles
    }

    String getComponentPath(ModuleComponentIdentifier id) {
        def gpath = id.group.replace('.', '/')
        "$gpath/$id.module/$id.version"
    }
    String getComponentPath(ModuleComponentIdentifier id, File file) {
        def path = getComponentPath(id)
        "$path/$file.name"
    }

    @InputFiles
    Set<File> getDependencyFiles() {
        new HashSet(resolveDepFiles().values())
    }

    @OutputFiles
    Collection<File> getOutputFiles() {
        def root = getRepoRoot()
        resolveDepFiles().entries().collect { e ->
            def path = getComponentPath(e.key, e.value)
            project.file("$root/$path")
        }
    }

    @TaskAction
    void republish() {
        def root = getRepoRoot()
        for (e in resolveDepFiles(false).asMap().entrySet()) {
            logger.info("copying {} artifacts for {}", e.value.size(), e.key)
            def path = getComponentPath(e.key)
            project.copy {
                from project.files(e.value.toList())
                into "$root/$path"
            }
        }
    }
}
