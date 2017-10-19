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
package org.lenskit.eval.traintest;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.Monitor;
import net.jcip.annotations.ThreadSafe;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.InjectionException;
import org.grouplens.grapht.graph.DAGEdge;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.graph.DAGNodeBuilder;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.grapht.reflect.SatisfactionVisitor;
import org.grouplens.grapht.reflect.Satisfactions;
import org.lenskit.inject.GraphtUtils;
import org.lenskit.inject.NodeInstantiator;
import org.lenskit.inject.NodeProcessor;
import org.lenskit.util.describe.*;
import org.lenskit.util.io.CustomClassLoaderObjectInputStream;
import org.lenskit.util.io.StagedWrite;
import org.lenskit.util.parallel.Blockers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;
import java.io.*;
import java.lang.ref.SoftReference;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
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
        logger.debug("resolving node {}", node);
        // We only want to process shareable nodes.
        if (!GraphtUtils.isShareable(node)) {
            logger.debug("node {} is not shareable", node);
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
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InjectionException("Cache load interrupted", e);
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
        private final Monitor monitor = new Monitor();
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
        public Object getObject(DAGNode<Component, Dependency> node) throws IOException, InjectionException, InterruptedException {
            Blockers.enterMonitor(monitor);
            try {
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
            } finally {
                monitor.leave();
            }
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
            } catch (ClosedByInterruptException | InterruptedIOException ex) {
                logger.info("Evaluation thread interrupted, aborting");
                Thread.currentThread().interrupt();
                throw new UncheckedIOException("Evaluation thread interrupted", ex);
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
                    node.getOutgoingEdges()
                    .stream()
                    .sorted(GraphtUtils.DEP_EDGE_ORDER)
                    .map(DAGEdge::getTail)
                    .collect(Collectors.toList());

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
