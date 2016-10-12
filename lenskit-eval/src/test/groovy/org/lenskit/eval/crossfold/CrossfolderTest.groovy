/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.eval.crossfold

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.java.quickcheck.Generator
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.lenskit.data.dao.BridgeEventDAO
import org.lenskit.data.dao.EventDAO
import org.lenskit.data.dao.file.StaticDataSource
import org.lenskit.data.entities.CommonAttributes
import org.lenskit.data.entities.CommonTypes
import org.lenskit.data.events.Event
import org.lenskit.data.output.OutputFormat
import org.lenskit.data.ratings.PreferenceDomain
import org.lenskit.data.ratings.Rating
import org.lenskit.eval.traintest.DataSet

import java.nio.file.Files

import static net.java.quickcheck.generator.PrimitiveGenerators.*
import static net.java.quickcheck.generator.iterable.Iterables.toIterable
import static org.grouplens.lenskit.util.test.ExtraMatchers.existingFile
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class CrossfolderTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder()

    private List<Rating> ratings
    private EventDAO sourceDAO
    private Crossfolder cf

    @Before
    public void createEvents() {
        ratings = []
        Generator<Integer> sizes = integers(20, 50);
        for (user in toIterable(longs(1, 1000000000), 100)) {
            for (item in toIterable(longs(), sizes.next())) {
                double rating = doubles(1,5).next()
                ratings << Rating.create(user, item, rating)
            }
        }
        def data = new StaticDataSource("test")
        data.addSource(ratings, [domain: [minimum: 1, maximum: 5]])
        sourceDAO = new BridgeEventDAO(data.get());
        cf = new Crossfolder()
        cf.source = data
        cf.setOutputDir(tmp.root)
    }

    @Test
    public void testFreshCFStateDoesNotHaveFiles() {
        assertThat(cf.name, equalTo("test"))
        assertThat(cf.partitionCount, equalTo(5))
        assertThat(cf.method, instanceOf(UserPartitionCrossfoldMethod))
        assertThat(cf.skipIfUpToDate, equalTo(false))
        assertThat(cf.writeTimestamps, equalTo(true))
        assertThat(cf.outputFormat, equalTo(OutputFormat.CSV))
        assertThat(Files.exists(tmp.root.toPath().resolve("datasets.yaml")),
                   equalTo(false))
        for (i in 1..5) {
            assertThat(Files.exists(tmp.root.toPath()
                                       .resolve(String.format("part%02d.csv", i))),
                       equalTo(false))
        }
    }

    @Test
    public void testFreshCFRun() {
        cf.execute()
        def dss = cf.dataSets
        assertThat(dss, hasSize(5))
        def allUsers = new LongOpenHashSet()
        for (ds in dss) {
            // test the users
            def users = ds.testData.get().getEntityIds(CommonTypes.USER)
            allUsers += users
            // each test set should have 20 users
            assertThat(users, hasSize(20))
            // train data should have all users
            assertThat(ds.trainingData.get().getEntityIds(CommonTypes.USER), hasSize(100))
            // each test user should have 10 ratings
            def dao = ds.testData.get()
            for (user in users) {
                assertThat(dao.query(CommonTypes.RATING)
                              .withAttribute(CommonAttributes.USER_ID, user)
                              .get(),
                           hasSize(10))
            }
        }
        assertThat(allUsers, hasSize(100))

        // Does the item file exist? It should for this kind of data
        assertThat(Files.exists(tmp.root.toPath().resolve("items.txt")),
                   equalTo(true));

        def dataSets = DataSet.load(tmp.root.toPath().resolve("datasets.yaml"))

        for (int i = 1; i <= 5; i++) {
            def train = tmp.root.toPath().resolve(String.format("part%02d.train.csv", i))
            assertThat(Files.exists(train), equalTo(true))
            def test = tmp.root.toPath().resolve(String.format("part%02d.test.csv", i))
            assertThat(Files.exists(test), equalTo(true))
            assertThat(tmp.root.toPath().resolve(String.format("part%02d.train.yaml", i)).toFile(),
                       existingFile())
            assertThat(tmp.root.toPath().resolve(String.format("part%02d.test.yaml", i)).toFile(),
                       existingFile())
            def obj = dataSets[i-1]
            assertThat(obj.trainingData.preferenceDomain,
                       equalTo(PreferenceDomain.fromString("[1,5]")))
            assertThat(obj.testData.preferenceDomain,
                       equalTo(PreferenceDomain.fromString("[1,5]")))

            // Can we load the train data properly?
            StaticDataSource trainP = StaticDataSource.load(tmp.root
                                                               .toPath()
                                                               .resolve(String.format("part%02d.train.yaml", i)));
            assertThat(trainP, notNullValue())
            def trainDao = trainP.get()
            // And does it have 100 users?
            assertThat(trainDao.getEntityIds(CommonTypes.USER),
                       hasSize(100))
            // And does it have an item data source?
            assertThat(trainP.getSourcesForType(CommonTypes.ITEM),
                       hasSize(1))

            // Can we load the test data properly?
            StaticDataSource testP = StaticDataSource.load(tmp.root
                                                              .toPath()
                                                              .resolve(String.format("part%02d.test.yaml", i)));
            assertThat(testP, notNullValue())
            def testDao = testP.get()
            // And does it have 20 users?
            assertThat(testDao.getEntityIds(CommonTypes.USER),
                       hasSize(20))
            // and does it just have 1 source?
            assertThat(testP.getSources(),
                       hasSize(1))
        }
    }

    @Test
    public void testDataSetListOutput() {
        cf.execute()
        def specFile = tmp.root.toPath().resolve("datasets.yaml")
        assertThat(Files.exists(specFile), equalTo(true))

        def datasets = DataSet.load(specFile)
        assertThat(datasets, hasSize(5))
    }

    @Test
    public void test10PartCFRun() {
        cf.partitionCount = 10
        cf.execute()
        def dss = cf.dataSets
        assertThat(dss, hasSize(10))
        def allUsers = new LongOpenHashSet()
        for (ds in dss) {
            // test the users
            def users = ds.testData.get().getEntityIds(CommonTypes.USER)
            allUsers += users
            // each test set should have 100/10 users
            assertThat(users, hasSize(10))
            // train data should have all users
            assertThat(ds.trainingData.get().getEntityIds(CommonTypes.USER), hasSize(100))
            // each test user should have 10 ratings
            def dao = ds.testData.get()
            for (user in users) {
                assertThat(dao.query(CommonTypes.RATING)
                              .withAttribute(CommonAttributes.USER_ID, user)
                              .get(),
                           hasSize(10))
            }
        }
        assertThat(allUsers, hasSize(100))

        def dataSets = DataSet.load(tmp.root.toPath().resolve("datasets.yaml"))
        assertThat(dataSets, hasSize(10))

        for (int i = 1; i <= 10; i++) {
            def train = tmp.root.toPath().resolve(String.format("part%02d.train.csv", i))
            assertThat(Files.exists(train), equalTo(true))
            def test = tmp.root.toPath().resolve(String.format("part%02d.test.csv", i))
            assertThat(Files.exists(test), equalTo(true))
        }
    }

    @Test
    public void testUserSample() {
        cf.method = CrossfoldMethods.sampleUsers(SortOrder.RANDOM, HistoryPartitions.holdout(5), 5);
        cf.execute()
        def dss = cf.dataSets
        assertThat(dss, hasSize(5))
        def allUsers = new LongOpenHashSet()
        for (ds in dss) {
            // test the users
            def users = ds.testData.get().getEntityIds(CommonTypes.USER)
            allUsers += users
            // each test set should have 5 users
            assertThat(users, hasSize(5))
            // train data should have all users
            assertThat(ds.trainingData.get().getEntityIds(CommonTypes.USER), hasSize(100))
            // each test user should have 5 ratings
            def dao = ds.testData.get()
            for (user in users) {
                assertThat(dao.query(CommonTypes.RATING)
                              .withAttribute(CommonAttributes.USER_ID, user)
                              .get(),
                           hasSize(5))
            }
        }
        assertThat(allUsers, hasSize(25))

        def dataSets = DataSet.load(tmp.root.toPath().resolve("datasets.yaml"))
        assertThat(dataSets, hasSize(5))

        for (int i = 1; i <= 5; i++) {
            def train = tmp.root.toPath().resolve(String.format("part%02d.train.csv", i))
            assertThat(Files.exists(train), equalTo(true))
            def test = tmp.root.toPath().resolve(String.format("part%02d.test.csv", i))
            assertThat(Files.exists(test), equalTo(true))
        }
    }

    @Test
    public void testPartitionRatings() {
        cf.method = CrossfoldMethods.partitionEntities()
        cf.execute()
        def dss = cf.dataSets
        assertThat(dss, hasSize(5))
        def allEvents = new HashSet<Event>();

        double perPart = ratings.size() / 5.0
        for (ds in dss) {
            // test the users
            def events = ds.testData.get().query(Rating.class).get()
            allEvents += events;

            assertThat(events, hasSize(allOf(greaterThanOrEqualTo((Integer) Math.floor(perPart)),
                                             lessThanOrEqualTo((Integer) Math.ceil(perPart)))));

            // train data should have all the other events
            def tes = ds.trainingData.get().query(Rating.class).get()
            assertThat(tes.size() + events.size(), equalTo(ratings.size()))
        }
        assertThat(allEvents, hasSize(ratings.size()))

        def dataSets = DataSet.load(tmp.root.toPath().resolve("datasets.yaml"))
        assertThat(dataSets, hasSize(5))

        for (int i = 1; i <= 5; i++) {
            def train = tmp.root.toPath().resolve(String.format("part%02d.train.csv", i))
            assertThat(Files.exists(train), equalTo(true))
            def test = tmp.root.toPath().resolve(String.format("part%02d.test.csv", i))
            assertThat(Files.exists(test), equalTo(true))
        }
    }

    @Test
    public void testUserTimestampOrder() {
        cf.method = CrossfoldMethods.partitionUsers(SortOrder.TIMESTAMP, HistoryPartitions.holdout(5));
        cf.execute()
        def dss = cf.dataSets
        assertThat(dss, hasSize(5))
        def allUsers = new LongOpenHashSet()
        for (ds in dss) {
            // test the users
            def users = ds.testData.get().getEntityIds(CommonTypes.USER)
            allUsers += users
            // each test set should have 20 users
            assertThat(users, hasSize(20))
            // train data should have all users
            assertThat(ds.trainingData.get().getEntityIds(CommonTypes.USER), hasSize(100))
            // each test user should have 10 ratings
            def dao = ds.testData.get()
            def trainDao = ds.trainingData.get()
            for (user in users) {
                def uevts = dao.query(Rating)
                               .withAttribute(CommonAttributes.USER_ID, user)
                               .get()
                def trainEvts = trainDao.query(Rating)
                                        .withAttribute(CommonAttributes.USER_ID, user)
                                        .get()
                def minTest = uevts*.timestamp.min()
                def maxTrain = trainEvts*.timestamp.max()
                assertThat(minTest, greaterThanOrEqualTo(maxTrain))
            }
        }
        assertThat(allUsers, hasSize(100))
    }

    @Test
    public void testRetainNPartition() {
        cf.method = CrossfoldMethods.partitionUsers(SortOrder.TIMESTAMP, HistoryPartitions.retain(5));
        cf.execute()
        def dss = cf.dataSets
        assertThat(dss, hasSize(5))
        def allUsers = new LongOpenHashSet()
        for (ds in dss) {
            // test the users
            def users = ds.testData.get().getEntityIds(CommonTypes.USER)
            allUsers += users
            // each test set should have 20 users
            assertThat(users, hasSize(20))
            // train data should have all users
            assertThat(ds.trainingData.get().getEntityIds(CommonTypes.USER), hasSize(100))
            // each test user should have 10 ratings
            def dao = ds.trainingData.get()
            for (user in users) {
                def uevts = dao.query(Rating)
                               .withAttribute(CommonAttributes.USER_ID, user)
                               .get()
                def trainEvts = ds.trainingData.get()
                                  .query(Rating.class)
                                  .withAttribute(CommonAttributes.USER_ID, user)
                                  .get()
                assertThat(trainEvts, hasSize(5));
                def minTest = uevts*.timestamp.min()
                def maxTrain = trainEvts*.timestamp.max()
                assertThat(minTest, greaterThanOrEqualTo(maxTrain))
            }
        }
        assertThat(allUsers, hasSize(100))
    }
}
