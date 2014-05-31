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
package org.grouplens.lenskit.util.io;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.grouplens.lenskit.util.Functional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.UUID;

/**
 * Utility classes for {@link AbstractDescriptionWriter}s.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 2.1
 */
public final class Descriptions {
    static Logger logger = LoggerFactory.getLogger(Descriptions.class);

    /**
     * Cache random UUIDs for objects.  The objects are weakly referenced and identified by identity.
     */
    private static final LoadingCache<Object,UUID> RANDOM_KEY_MAP =
            CacheBuilder.newBuilder()
                        .weakKeys()
                        .build(new CacheLoader<Object, UUID>() {
                            @Override
                            public UUID load(Object key) throws Exception {
                                return UUID.randomUUID();
                            }
                        });

    private Descriptions() {}

    /**
     * Create a description writer that will compute a SHA1 hash.
     *
     * @return A description writer computing a SHA1 hash.
     */
    public static HashDescriptionWriter sha1Writer() {
        return hashWriter(Hashing.sha1());
    }

    /**
     * Create a description writer for a particular hash function.
     * @param func The hash function.
     * @return A description writer that computes a hash using {@code func}.
     */
    public static HashDescriptionWriter hashWriter(HashFunction func) {
        return new HashDescriptionWriter(func.newHasher());
    }

    /**
     * Construct a new description writer that outputs a string.
     * @return A string description writer.
     */
    public static StringDescriptionWriter stringWriter() {
        return new StringDescriptionWriter();
    }

    /**
     * Get a default describer.  This describer uses the following algorithm:
     * <ol>
     * <li>If the object is {@link Describable}, call its its {@link Describable#describeTo(DescriptionWriter)} method.</li>
     * <li>If the object is {@link java.io.Serializable}, write its class and a SHA1 checksum of
     * its serialized form to the description.</li>
     * <li>Otherwise, generate a random {@link java.util.UUID} as the object's description.  This
     * UUID is remembered, so the same object will have the same UUID within a single instance of
     * the JVM.</li>
     * </ol>
     *
     * @return A describer implementing the logic above.
     */
    public static Describer<Object> defaultDescriber() {
        return DefaultDescriber.INSTANCE;
    }

    private static enum DefaultDescriber implements Describer<Object> {
        INSTANCE;

        @Override
        public void describe(Object obj, DescriptionWriter description) {
            if (obj == null) {
                description.putField("type", "null");
                return;
            }
            description.putField("type", obj.getClass().getName());
            if (obj instanceof String) {
                // FIXME Do something saner here - just write the string somehow
                description.putField("string", (String) obj);
            } else if (obj instanceof Number) {
                description.putField("number", obj.toString());
            } if (obj instanceof Describable) {
                ((Describable) obj).describeTo(description);
            } else if (obj instanceof Serializable) {
                logger.debug("describing {} by hashing its serialization", obj);
                HashCode hash = Hashing.sha1().hashObject(obj, Functional.serializeFunnel());
                description.putField("hash", hash.toString());
            } else {
                logger.warn("object {} not describable or serializable, using nondeterministic key",
                            obj);
                UUID key = RANDOM_KEY_MAP.getUnchecked(obj);
                description.putField("_uuid", key.toString());
            }
        }
    }
}
