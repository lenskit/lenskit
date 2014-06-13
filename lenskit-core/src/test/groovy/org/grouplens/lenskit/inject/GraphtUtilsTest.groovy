/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.inject

import org.grouplens.grapht.CachePolicy
import org.grouplens.grapht.Component
import org.grouplens.grapht.graph.DAGNode
import org.grouplens.grapht.reflect.Satisfactions
import org.grouplens.lenskit.core.Shareable
import org.junit.Test

import javax.inject.Provider

import static org.junit.Assert.*
import static org.hamcrest.Matchers.*

class GraphtUtilsTest {

    @Test
    public void testShareableImpl() {
        def node = DAGNode.newBuilder(Component.create(Satisfactions.type(ShareableImpl),
                                                       CachePolicy.NO_PREFERENCE))
                          .build()
        assertThat GraphtUtils.isShareable(node), equalTo(true)
    }

    @Test
    public void testUnshareableImpl() {
        def node = DAGNode.newBuilder(Component.create(Satisfactions.type(UnshareableImpl),
                                                       CachePolicy.NO_PREFERENCE))
                          .build()
        assertThat GraphtUtils.isShareable(node), equalTo(false)
    }

    @Test
    public void testShareableInstance() {
        def node = DAGNode.newBuilder(Component.create(Satisfactions.instance(new ShareableImpl()),
                                                       CachePolicy.NO_PREFERENCE))
                          .build()
        assertThat GraphtUtils.isShareable(node), equalTo(true)
    }

    @Test
    public void testUnshareableInstanceIsShareable() {
        def node = DAGNode.newBuilder(Component.create(Satisfactions.instance("hello, world"),
                                                       CachePolicy.NO_PREFERENCE))
                          .build()
        // instances are always shareable
        assertThat GraphtUtils.isShareable(node), equalTo(true)
    }

    @Test
    public void testShareableProvider() {
        def node = DAGNode.newBuilder(Component.create(Satisfactions.providerType(ShareableProvider),
                                                       CachePolicy.NO_PREFERENCE))
                          .build()
        assertThat GraphtUtils.isShareable(node), equalTo(true)
    }

    @Test
    public void testShareableProviderInstance() {
        def node = DAGNode.newBuilder(Component.create(Satisfactions.providerInstance(new ShareableProvider()),
                                                       CachePolicy.NO_PREFERENCE))
                          .build()
        assertThat GraphtUtils.isShareable(node), equalTo(true)
    }

    @Test
    public void testUnshareableProvider() {
        def node = DAGNode.newBuilder(Component.create(Satisfactions.providerType(UnshareableProvider),
                                                       CachePolicy.NO_PREFERENCE))
                          .build()
        assertThat GraphtUtils.isShareable(node), equalTo(false)
    }

    @Test
    public void testUnshareableProviderInstance() {
        def node = DAGNode.newBuilder(Component.create(Satisfactions.providerInstance(new UnshareableProvider()),
                                                       CachePolicy.NO_PREFERENCE))
                          .build()
        assertThat GraphtUtils.isShareable(node), equalTo(false)
    }

    @Shareable
    private static class ShareableImpl {}

    private static class UnshareableImpl {}

    private static class ShareableProvider implements Provider<List> {
        @Override @Shareable
        List get() {
            return "hackem muche";
        }
    }

    private static class UnshareableProvider implements Provider<List> {
        @Override
        List get() {
            return "hackem muche";
        }
    }
}
