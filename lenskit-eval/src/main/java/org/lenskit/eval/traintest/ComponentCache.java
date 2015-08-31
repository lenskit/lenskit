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
package org.lenskit.eval.traintest;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.InjectionException;
import org.grouplens.grapht.graph.DAGEdge;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.graph.DAGNodeBuilder;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.grapht.reflect.SatisfactionVisitor;
import org.grouplens.grapht.reflect.Satisfactions;
import org.grouplens.lenskit.inject.GraphtUtils;
import org.grouplens.lenskit.inject.NodeInstantiator;
import org.grouplens.lenskit.inject.NodeProcessor;
import org.grouplens.lenskit.util.io.*;
import org.lenskit.util.io.StagedWrite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Provider;
import java.io.*;
import java.lang.ref.SoftReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Shared cache for components in merged compilations.  This cache implements two kinds of caching for shareable nodes:
 *
 * -   Soft references to allow instances to be opportunistically reused between invocations
 * -   Optional disk-based caching to allow shareable components to be shared between all uses, even if their soft
 *     references might be flushed out of memory, as well as reused by subsequent evaluator invocations.
 */
@ThreadSafe
class ComponentCache implements NodeProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ComponentCache.class);

    @Nullable
    private final Path cacheDir;
    
    @Nullable
    private final ClassLoader classLoader;

    private NodeInstantiator instantiator;

    /**
     * The set of cacheable nodes.
     */
    private final ConcurrentHashMap<DAGNode<Component,Dependency>,CacheEntry> cache;

    /**
     * Construct a new component cache.
     *
     * @param dir The cache directory (or {@code null} to disable disk-based caching).
     * @param loader The class loader to be used when loading components from disk (or {@code null} if not needed).
     */
    public ComponentCache(@Nullable Path dir, @Nullable ClassLoader loader) {
        cacheDir = dir;
        classLoader = loader;
        instantiator = NodeInstantiator.create();
        cache = new ConcurrentHashMap<>();
    }

    @Nullable
    public Path getCacheDir() {
        return cacheDir;
    }

    Object instantiate(@Nonnull DAGNode<Component, Dependency> node) throws InjectionException {
        DAGNode<Component,Dependency> n2 = processNode(node, node);
        return instantiator.instantiate(n2);
    }

    @Nonnull
    @Override
    public DAGNode<Component, Dependency> processNode(@Nonnull DAGNode<Component, Dependency> node,
                                                      @Nonnull DAGNode<Component, Dependency> original) throws InjectionException {
        // We only want to process shareable nodes.
        if (!GraphtUtils.isShareable(node)) {
            return node;
        }

        // Is the object pre-instantiated?  If so, we will skip the caching logic.
        Component label = node.getLabel();
        Satisfaction satisfaction = label.getSatisfaction();
        if (satisfaction.hasInstance()) {
            return node;
        }

        // Make sure we have a cache entry for this node.
        CacheEntry newEntry = new CacheEntry(node);
        CacheEntry entry = cache.putIfAbsent(original, newEntry);
        if (entry == null) {
            // didn't find an entry, but inserted the new one
            entry = newEntry;
        }

        // Now try to get the instantiated object from the cache
        Object obj;
        try {
            obj = entry.getObject(node);
        } catch (IOException e) {
            throw new InjectionException("Cache I/O error", e);
        }

        // Build a new satisfaction and a node, with all non-transient edges.
        Satisfaction instanceSat;
        if (obj == null) {
            instanceSat = Satisfactions.nullOfType(satisfaction.getErasedType());
        } else {
            instanceSat = Satisfactions.instance(obj);
        }
        Component newLabel = Component.create(instanceSat, label.getCachePolicy());
        // build new node with replacement label
        DAGNodeBuilder<Component,Dependency> bld = DAGNode.newBuilder(newLabel);
        // retain all non-transient edges
        for (DAGEdge<Component, Dependency> edge: node.getOutgoingEdges()) {
            if (!GraphtUtils.edgeIsTransient(edge)) {
                bld.addEdge(edge.getTail(), edge.getLabel());
            }
        }
        return bld.build();
    }

    /**
     * Class with the cache entry logic.
     */
    private class CacheEntry {
        private final String key;
        // reference *inside* optional so we don't GC the optional while keeping the object
        // this is null for uncached, empty for cached null
        private Optional<SoftReference<Object>> cachedObject;

        /**
         * Createa a cache entry.
         * @param n The node.
         */
        public CacheEntry(DAGNode<Component, Dependency> n) {
            key = makeNodeKey(n);
        }

        @Nullable
        private Path getCacheFile() {
            if (cacheDir == null) {
                return null;
            } else {
                return cacheDir.resolve(key + ".dat.gz");
            }
        }

        /**
         * Get the object.
         * @param node The live version of the node.  It must be compatible with the node used
         *             to create this object!  That means that it should be the same node, or a
         *             mutation of the node.
         * @return The object loaded from the cache, or from instantiating {@code node}.
         * @throws IOException If there is an I/O error with the cache.
         */
        public synchronized Object getObject(DAGNode<Component, Dependency> node) throws IOException, InjectionException {
            // check soft-reference cache
            Optional<Object> cached = getMemoryCachedObject();
            if (cached != null) {
                logger.debug("reusing {} from memory", cached);
                return cached.orNull();
            }

            // Either we have not cached the object, or the cache has left memory
            Path cacheFile = getCacheFile();
            cached = getDiskCachedObject(cacheFile, node);
            if (cached != null) {
                return cached.orNull();
            }

            // No object from the serialization stream, let's try to make one
            logger.debug("instantiating object for {}", node.getLabel().getSatisfaction());
            Object result = instantiator.instantiate(node);
            if (result == null) {
                // cache the result
                cachedObject = Optional.absent();
            } else {
                cachedObject = Optional.of(new SoftReference<>(result));
            }

            // now save it to disk, if possible and non-null
            writeDiskCache(result, cacheFile, node);

            return result;
        }


        /**
         * Get the cached object.
         *
         * @return The cached object, or {@code null} if the cache is invalid.  A value of {@link Optional#absent()}
         * indicates a cached null.
         */
        @Nullable
        private Optional<Object> getMemoryCachedObject() {
            Optional<Object> result = null;
            if (cachedObject != null) {
                if (cachedObject.isPresent()) {
                    Object obj = cachedObject.get().get();
                    if (obj != null) {
                        result = Optional.of(obj);
                    }
                } else {
                    result = Optional.absent();
                }
            }

            return result;
        }

        @Nullable
        private Optional<Object> getDiskCachedObject(Path file, DAGNode<Component,Dependency> node) {
            if (file != null && Files.exists(file)) {
                logger.debug("reading object for {} from cache (key {})",
                             node.getLabel().getSatisfaction(), key);
                Object obj = readCompressedObject(file, node.getLabel().getSatisfaction().getErasedType());
                logger.debug("read object {} from key {}", obj, key);
                return Optional.fromNullable(obj);
            } else {
                return null;
            }
        }

        private void writeDiskCache(Object obj, Path file, DAGNode<Component, Dependency> node) throws IOException {
            if (obj != null) {
                if (obj instanceof Serializable) {
                    if (file != null) {
                        logger.debug("writing object {} to cache (key {})",
                                     obj, key);
                        if (logger.isDebugEnabled()) {
                            StringDescriptionWriter sdw = Descriptions.stringWriter();
                            NodeDescriber.INSTANCE.describe(node, sdw);
                            logger.debug("object description: {}", sdw.finish());
                        }
                        writeCompressedObject(file, obj);
                        logger.info("wrote object {} to cache as {} ({} bytes)",
                                    obj, key, Files.size(file));
                    }
                } else {
                    logger.warn("object {} is not serializable, not caching", obj);
                }
            }
        }

        private void writeCompressedObject(Path cacheFile, Object obj) throws IOException {
            assert cacheDir != null;
            Files.createDirectories(cacheDir);
            try (StagedWrite stage = StagedWrite.begin(cacheFile)) {
                try (OutputStream out = stage.openOutputStream();
                     OutputStream gzOut = new GZIPOutputStream(out);
                     ObjectOutputStream objOut = new ObjectOutputStream(gzOut)) {
                    objOut.writeObject(obj);
                }
                // now we commit, after closing the output files
                stage.commit();
            }
        }

        private Object readCompressedObject(Path cacheFile, Class<?> type) {
            // The file is there, load it
            try (InputStream in = Files.newInputStream(cacheFile, StandardOpenOption.READ);
                 InputStream gzin = new GZIPInputStream(in);
                 ObjectInputStream oin = new CustomClassLoaderObjectInputStream(gzin, classLoader)) {

                return type.cast(oin.readObject());
            } catch (IOException ex) {
                logger.warn("ignoring cache file {} due to read error: {}",
                            cacheFile.getFileName(), ex.toString());
                logger.info("This error can be caused by a corrupted cache file.");
                return null;
            } catch (ClassNotFoundException ex) {
                logger.warn("ignoring cache file {} due to read error: {}",
                            cacheFile.getFileName(), ex.toString());
                logger.info("This error can be caused by a corrupted cache file.");
                return null;
            }
        }
    }

    //region Node key generation

    static String makeNodeKey(DAGNode<Component, Dependency> node) {
        HashDescriptionWriter descr = Descriptions.sha1Writer();
        NodeDescriber.INSTANCE.describe(node, descr);
        return descr.finish().toString();
    }

    /**
     * A describer for graph nodes, for generating keys of configuration subgraphs.
     */
    private enum NodeDescriber implements Describer<DAGNode<Component, Dependency>> {
        INSTANCE;

        @Override
        public void describe(DAGNode<Component, Dependency> node, DescriptionWriter description) {
            node.getLabel().getSatisfaction().visit(new LabelDescriptionVisitor(description));
            description.putField("cachePolicy", node.getLabel().getCachePolicy().name());
            List<DAGNode<Component, Dependency>> edges =
                    Lists.transform(GraphtUtils.DEP_EDGE_ORDER.sortedCopy(node.getOutgoingEdges()),
                                    DAGEdge.<Component,Dependency>extractTail());
            description.putList("dependencies", edges, INSTANCE);
        }
    }

    /**
     * Satisfaction visitor for hashing node satisfactions (objects, etc.)
     */
    private static class LabelDescriptionVisitor implements SatisfactionVisitor<String> {
        private final DescriptionWriter description;

        public LabelDescriptionVisitor(DescriptionWriter sink) {
            description = sink;
        }

        @Override
        public String visitNull() {
            description.putField("type", "null");
            return "";
        }

        @Override
        public String visitClass(Class<?> clazz) {
            description.putField("type", "class")
                       .putField("class", clazz.getCanonicalName());
            return clazz.getCanonicalName();
        }

        @Override
        public String visitInstance(Object instance) {
            description.putField("type", "instance")
                       .putField("object", instance);
            return instance.toString();
        }

        @Override
        public String visitProviderClass(Class<? extends Provider<?>> pclass) {
            description.putField("type", "provider class")
                       .putField("class", pclass.getCanonicalName());
            return pclass.getCanonicalName();
        }

        @Override
        public String visitProviderInstance(Provider<?> provider) {
            if (provider == null) {
                description.putField("type", "null provider");
                return "null provider";
            } else {
                description.putField("type", "provider")
                           .putField("provider", provider);
                return provider.toString();
            }
        }
    }
    //endregion
}
