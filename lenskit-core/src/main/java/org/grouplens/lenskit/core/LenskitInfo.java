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
package org.grouplens.lenskit.core;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.ref.SoftReference;
import java.util.Properties;
import java.util.Set;

/**
 * Get access to general information about LensKit.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class LenskitInfo {
    private static final Logger logger = LoggerFactory.getLogger(LenskitInfo.class);
    private static SoftReference<Set<String>> revisionSet;

    private static Set<String> loadRevisionSet() {
        ImmutableSet.Builder<String> revisions = ImmutableSet.builder();
        InputStream input = LenskitInfo.class.getResourceAsStream("/META-INF/lenskit/git-commits.lst");
        if (input != null) {
            try {
                Reader reader = new InputStreamReader(input);
                BufferedReader lines = new BufferedReader(reader);
                String line;
                while ((line = lines.readLine()) != null) {
                    revisions.add(StringUtils.trim(line));
                }
            } catch (IOException e) {
                throw new RuntimeException("error reading revision list", e);
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                    logger.error("error closing git-commit list", e);
                }
            }
        } else {
            logger.warn("cannot find LensKit revision list");
        }
        Set<String> revset = revisions.build();
        logger.debug("have {} active revisions", revset.size());
        return revset;
    }

    /**
     * Get the set of revisions LensKit is built from.  This is in the order returned by {@code git log},
     * so the head revision is first.
     * @return The set of revisions included in this build of LensKit.
     */
    public static synchronized Set<String> getRevisions() {
        Set<String> revisions = revisionSet == null ? null : revisionSet.get();
        if (revisions == null) {
            revisions = loadRevisionSet();
            revisionSet = new SoftReference<Set<String>>(revisions);
        }
        return revisions;
    }

    /**
     * Query whether this version of LensKit includes a particular revision.
     *
     * @param revision The revision to query.
     * @return {@code true} if the LensKit source is descended from {@code revision}.
     */
    public static boolean includesRevision(String revision) {
        return getRevisions().contains(revision);
    }

    /**
     * Get the HEAD revision from which LensKit was built.
     * @return The revision from which this version of LensKit was built.
     */
    public static String getHeadRevision() {
        return getRevisions().iterator().next();
    }

    /**
     * Get the current LensKit version.
     * @return The LensKit version.
     */
    public static String lenskitVersion() {
        Properties props = new Properties();
        InputStream stream = LenskitInfo.class.getResourceAsStream("/META-INF/lenskit/version.properties");
        try {
            props.load(stream);
        } catch (IOException e) {
            throw new RuntimeException("properties error", e);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return props.getProperty("lenskit.version");
    }
}
