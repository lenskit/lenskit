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
package org.grouplens.lenskit.eval.traintest;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Closer;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Provider;
import java.io.*;
import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Shared cache for components in merged compilations.  This is a node processor, instantiating
 * objects using the cache.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@ThreadSafe
class ComponentCache implements NodeProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ComponentCache.class);

    @Nullable
    private final File cacheDir;
    
    @Nullable
    private final ClassLoader classLoader;

    private NodeInstantiator instantiator;

    /**
     * The set of cacheable nodes.
     */
    private final Map<DAGNode<Component,Dependency>,CacheEntry> cache;

    /**
     * Construct a new component cache.
     *
     * @param dir The cache directory (or {@code null} to disable disk-based caching).
     * @param loader The class loader to be used when loading components from disk (or {@code null} if not needed)
     */
    public ComponentCache(@Nullable File dir, @Nullable ClassLoader loader) {
        cacheDir = dir;
        classLoader = loader;
        instantiator = NodeInstantiator.create();
        cache = new WeakHashMap<DAGNode<Component, Dependency>, CacheEntry>();
    }

    @SuppressWarnings("unused")
    @Nullable
    public File getCacheDir() {
        return cacheDir;
    }

    /**
     * Register nodes that are shared (needed by multiple graphs) and should therefore be cached.
     *
     * @param nodes A collection of nodes that should be cached.
     */
    public void registerSharedNodes(Iterable<DAGNode<Component, Dependency>> nodes) {
        synchronized (cache) {
            for (DAGNode<Component,Dependency> node: nodes) {
                if (GraphtUtils.isShareable(node)) {
                    if (!cache.containsKey(node)) {
                        logger.debug("enabling caching for {}", node);
                        cache.put(node, new CacheEntry(node, true));
                    } else {
                        logger.debug("{} already has a cache entry", node);
                    }
                } else {
                    logger.debug("node {} not shareable, caching not enabled", node);
                }
            }
        }
    }

    /**
     * Register a node that is shared (needed by multiple graphs) and should therefore be cached.
     *
     * @param node A node that should be cached.
     */
    public void registerSharedNode(DAGNode<Component, Dependency> node) {
        registerSharedNodes(ImmutableList.of(node));
    }

    Object instantiate(@Nonnull DAGNode<Component, Dependency> node) throws InjectionException {
        CacheEntry entry;
        synchronized (cache) {
            entry = cache.get(node);
        }
        if (entry == null) {
            return instantiator.instantiate(node);
        } else {
            try {
                return entry.getObject(node);
            } catch (IOException e) {
                throw new RuntimeException("Cache I/O error", e);
            }
        }
    }

    @Nonnull
    @Override
    public DAGNode<Component, Dependency> processNode(@Nonnull DAGNode<Component, Dependency> node,
                                                      @Nonnull DAGNode<Component, Dependency> original) throws InjectionException {
        CacheEntry entry;
        synchronized (cache) {
            entry = cache.get(original);
        }
        if (entry == null) {
            // don't save to disk if it isn't shared
            entry = new CacheEntry(node, false);
        }

        Component label = node.getLabel();
        Satisfaction satisfaction = label.getSatisfaction();
        if (satisfaction.hasInstance()) {
            return node;
        }
        Object obj;
        try {
            obj = entry.getObject(node);
        } catch (IOException e) {
            throw new RuntimeException("Cache I/O error", e);
        }

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

    private class CacheEntry {
        private final String key;
        private final boolean tryDisk;
        // reference *inside* optional so we don't GC the optional while keeping the object
        // this is null for uncached, empty for cached null
        private Optional<SoftReference<Object>> cachedObject;

        /**
         * Createa a cache entry.
         * @param n The node.
         * @param disk Whether to try to store on disk.
         */
        public CacheEntry(DAGNode<Component, Dependency> n, boolean disk) {
            key = makeNodeKey(n);
            tryDisk = disk;
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
            if (cachedObject != null) {
                if (cachedObject.isPresent()) {
                    Object obj = cachedObject.get().get();
                    if (obj != null) {
                        logger.debug("object for {} cached in memory", node);
                        return obj;
                    }
                } else {
                    return null;
                }
            }

            File cacheFile = null;
            if (tryDisk && cacheDir != null) {
                cacheFile = new File(cacheDir, key + ".dat.gz");
                if (cacheFile.exists()) {
                    logger.debug("reading object for {} from cache (key {})",
                                 node.getLabel().getSatisfaction(), key);
                    Object obj = readCompressedObject(cacheFile, node.getLabel().getSatisfaction().getErasedType());
                    logger.debug("read object {} from key {}", obj, key);
                    return obj;
                }
            }

            // No object from the serialization stream, let's try to make one
            logger.debug("instantiating object for {}", node.getLabel().getSatisfaction());
            Object result = instantiator.instantiate(node);
            if (result == null) {
                // cache the result
                cachedObject = Optional.absent();
            } else {
                cachedObject = Optional.of(new SoftReference<Object>(result));
            }

            // now save it to disk, if possible and non-null
            if (result != null) {
                if (result instanceof Serializable) {
                    if (cacheFile != null) {
                        logger.debug("writing object {} to cache (key {})",
                                     result, key);
                        if (logger.isDebugEnabled()) {
                            StringDescriptionWriter sdw = Descriptions.stringWriter();
                            NodeDescriber.INSTANCE.describe(node, sdw);
                            logger.debug("object description: {}", sdw.finish());
                        }
                        writeCompressedObject(cacheFile, result);
                        logger.info("wrote object {} to cache as {} ({} bytes)",
                                    result, key, cacheFile.length());
                    }
                } else {
                    logger.warn("object {} is unserializable, not caching", result);
                }
            }

            return result;
        }

        private void writeCompressedObject(File cacheFile, Object obj) throws IOException {
            assert cacheDir != null;
            if (cacheDir.mkdirs()) {
                logger.debug("created cache directory {}", cacheDir);
            }
            StagedWrite stage = StagedWrite.begin(cacheFile);
            try {
                Closer closer = Closer.create();
                try {
                    OutputStream out = closer.register(stage.openOutputStream());
                    OutputStream gzOut = closer.register(new GZIPOutputStream(out));
                    ObjectOutputStream objOut = closer.register(new ObjectOutputStream(gzOut));
                    objOut.writeObject(obj);
                } catch (Throwable th) { // NOSONAR using a closer
                    throw closer.rethrow(th);
                } finally {
                    closer.close();
                }
                stage.commit();
            } finally {
                stage.close();
            }
        }

        private Object readCompressedObject(File cacheFile, Class<?> type) {
            // The file is there, load it
            try {
                Closer closer = Closer.create();
                try {
                    InputStream in = closer.register(new FileInputStream(cacheFile));
                    InputStream gzin = closer.register(new GZIPInputStream(in));
                    ObjectInputStream oin = closer.register(new CustomClassLoaderObjectInputStream(gzin, classLoader));
                    return type.cast(oin.readObject());
                } catch (Throwable th) { // NOSONAR using a closer
                    throw closer.rethrow(th);
                } finally {
                    closer.close();
                }
            } catch (IOException ex) {
                logger.warn("ignoring cache file {} due to read error: {}",
                            cacheFile.getName(), ex.toString());
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
    private static enum NodeDescriber implements Describer<DAGNode<Component, Dependency>> {
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
