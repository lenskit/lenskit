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
package org.lenskit.inject

import org.grouplens.grapht.CachePolicy
import org.grouplens.grapht.Component
import org.grouplens.grapht.graph.DAGNode
import org.grouplens.grapht.reflect.Satisfactions
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
