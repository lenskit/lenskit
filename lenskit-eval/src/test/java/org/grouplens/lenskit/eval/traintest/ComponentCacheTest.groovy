package org.grouplens.lenskit.eval.traintest

import org.grouplens.lenskit.ItemScorer
import org.grouplens.lenskit.baseline.ItemMeanRatingItemScorer
import org.grouplens.lenskit.config.ConfigHelpers
import org.grouplens.lenskit.data.dao.EventCollectionDAO
import org.grouplens.lenskit.data.dao.EventDAO
import org.grouplens.lenskit.data.event.Ratings
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.grouplens.lenskit.util.test.ExtraMatchers.existingFile
import static org.hamcrest.Matchers.notNullValue
import static org.hamcrest.Matchers.nullValue
import static org.hamcrest.Matchers.sameInstance
import static org.junit.Assert.assertThat

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class ComponentCacheTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder()
    def dao = EventCollectionDAO.create([
            Ratings.make(1, 10, 3.5),
            Ratings.make(1, 11, 2.5),
            Ratings.make(2, 10, 3.0)
    ])

    ComponentCache cache
    def config = ConfigHelpers.load {
        bind ItemScorer to ItemMeanRatingItemScorer
        bind EventDAO to dao
    }

    @Before
    void createCache() {
        cache = new ComponentCache(folder.root)
    }

    @Test
    public void testCacheObject() {
        def graph = config.buildGraph()
        def node = graph.reachableNodes.find {
            it.label.satisfaction.type == ItemMeanRatingItemScorer
        }
        def object = cache.makeInstantiator(graph).apply(node)
        def other = cache.makeInstantiator(graph).apply(node)
        assertThat object,
                   sameInstance(other)
        assertThat new File(folder.root, "${cache.getKey(node)}.dat.gz"),
                   existingFile()
    }

    @Test
    public void testReadDiskCache() {
        def graph = config.buildGraph()
        def node = graph.reachableNodes.find {
            it.label.satisfaction.type == ItemMeanRatingItemScorer
        }
        def object = cache.makeInstantiator(graph).apply(node)
        cache.objectCache.invalidateAll()
        def other = cache.makeInstantiator(graph).apply(node)
        // this doesn't really test that it was actually read from the file
        // that involves more custom classes
        assertThat other, notNullValue()
    }

    @Test
    public void testConfigureNull() {
        def config = ConfigHelpers.load {
            bind ItemScorer to ItemMeanRatingItemScorer
            bind ItemMeanRatingItemScorer to null
            bind EventDAO to dao
        }
        def graph = config.buildGraph()
        def node = graph.reachableNodes.find {
            it.label.satisfaction.type == ItemMeanRatingItemScorer
        }
        def object = cache.makeInstantiator(graph).apply(node)
        assertThat object, nullValue()
    }
}
