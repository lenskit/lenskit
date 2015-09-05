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
package org.lenskit.eval.crossfold

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.java.quickcheck.Generator
import org.grouplens.lenskit.data.dao.EventCollectionDAO
import org.grouplens.lenskit.data.dao.EventDAO
import org.grouplens.lenskit.data.source.DataSource
import org.grouplens.lenskit.data.source.GenericDataSource
import org.grouplens.lenskit.data.source.TextDataSource
import org.grouplens.lenskit.data.text.TextEventDAO
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.lenskit.data.events.Event
import org.lenskit.data.ratings.Rating
import org.lenskit.eval.traintest.DataSet
import org.lenskit.specs.SpecUtils
import org.lenskit.specs.eval.OutputFormat
import org.lenskit.specs.eval.TTDataSetSpec
import org.lenskit.util.io.ObjectStreams

import java.nio.file.Files

import static net.java.quickcheck.generator.PrimitiveGenerators.*
import static net.java.quickcheck.generator.iterable.Iterables.toIterable
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class CrossfolderTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder()

    private List<Rating> ratings
    private EventDAO sourceDAO
    private DataSource source
    private Crossfolder cf

    @Before
    public void createEvents() {
        ratings = []
        Generator<Integer> sizes = integers(20, 50);
        for (user in toIterable(longs(), 100)) {
            for (item in toIterable(longs(), sizes.next())) {
                double rating = doubles().next()
                ratings << Rating.create(user, item, rating)
            }
        }
        sourceDAO = EventCollectionDAO.create(ratings)
        source = new GenericDataSource("test", sourceDAO)
        cf = new Crossfolder()
        cf.source = source
        cf.setOutputDir(tmp.root)
    }

    @Test
    public void testFreshCFState() {
        assertThat(cf.name, equalTo("test"))
        assertThat(cf.partitionCount, equalTo(5))
        assertThat(cf.method, instanceOf(UserPartitionSplitMethod))
        assertThat(cf.skipIfUpToDate, equalTo(false))
        assertThat(cf.writeTimestamps, equalTo(true))
        assertThat(cf.outputFormat, equalTo(OutputFormat.CSV))
        def dss = cf.dataSets
        assertThat(dss, hasSize(5))
        for (ds in dss) {
            def dao = ds.trainingData.eventDAO as TextEventDAO
            assertThat(dao.inputFile.exists(), equalTo(false))
            assertThat(dao.inputFile.name, endsWith(".csv"))
        }
    }

    @Test
    public void testFreshCFRun() {
        cf.execute()
        def dss = cf.dataSets
        assertThat(dss, hasSize(5))
        def allUsers = new LongOpenHashSet()
        for (ds in dss) {
            def train = ds.trainingData.eventDAO as TextEventDAO
            def test = ds.testData.eventDAO as TextEventDAO
            assertThat(train.inputFile.exists(), equalTo(true))
            assertThat(test.inputFile.exists(), equalTo(true))

            // test the users
            def users = ds.testData.userDAO.userIds
            allUsers += users
            // each test set should have 20 users
            assertThat(users, hasSize(20))
            // train data should have all users
            assertThat(ds.trainingData.userDAO.userIds, hasSize(100))
            // each test user should have 10 ratings
            def ued = ds.testData.userEventDAO
            for (user in users) {
                assertThat(ued.getEventsForUser(user), hasSize(10))
            }
        }
        assertThat(allUsers, hasSize(100))

        for (int i = 1; i <= 5; i++) {
            def train = tmp.root.toPath().resolve(String.format("part%02d.train.csv", i))
            assertThat(Files.exists(train), equalTo(true))
            def test = tmp.root.toPath().resolve(String.format("part%02d.test.csv", i))
            assertThat(Files.exists(test), equalTo(true))
            def specFile = tmp.root.toPath().resolve(String.format("part%02d.json", i))
            assertThat(Files.exists(specFile), equalTo(true))
            def spec = SpecUtils.load(TTDataSetSpec, specFile)
            def obj = DataSet.fromSpec(spec)
            assertThat(obj.trainingData, instanceOf(TextDataSource))
            assertThat(obj.testData, instanceOf(TextDataSource))
            assertThat(obj.queryData, nullValue())
            assertThat(obj.trainingData.eventDAO.inputFile, equalTo(dss[i-1].trainingData.eventDAO.inputFile))
            assertThat(obj.testData.eventDAO.inputFile, equalTo(dss[i-1].testData.eventDAO.inputFile))
        }
    }

    @Test
    public void test10PartCFRun() {
        cf.partitionCount = 10
        cf.execute()
        def dss = cf.dataSets
        assertThat(dss, hasSize(10))
        def allUsers = new LongOpenHashSet()
        for (ds in dss) {
            def train = ds.trainingData.eventDAO as TextEventDAO
            def test = ds.testData.eventDAO as TextEventDAO
            assertThat(train.inputFile.exists(), equalTo(true))
            assertThat(test.inputFile.exists(), equalTo(true))

            // test the users
            def users = ds.testData.userDAO.userIds
            allUsers += users
            // each test set should have 100/10 users
            assertThat(users, hasSize(10))
            // train data should have all users
            assertThat(ds.trainingData.userDAO.userIds, hasSize(100))
            // each test user should have 10 ratings
            def ued = ds.testData.userEventDAO
            for (user in users) {
                assertThat(ued.getEventsForUser(user), hasSize(10))
            }
        }
        assertThat(allUsers, hasSize(100))
        for (int i = 1; i <= 10; i++) {
            def train = tmp.root.toPath().resolve(String.format("part%02d.train.csv", i))
            assertThat(Files.exists(train), equalTo(true))
            def test = tmp.root.toPath().resolve(String.format("part%02d.test.csv", i))
            assertThat(Files.exists(test), equalTo(true))
            def specFile = tmp.root.toPath().resolve(String.format("part%02d.json", i))
            assertThat(Files.exists(specFile), equalTo(true))
            def spec = SpecUtils.load(TTDataSetSpec, specFile)
            def obj = DataSet.fromSpec(spec)
            assertThat(obj.trainingData, instanceOf(TextDataSource))
            assertThat(obj.testData, instanceOf(TextDataSource))
            assertThat(obj.queryData, nullValue())
            assertThat(obj.trainingData.eventDAO.inputFile, equalTo(dss[i-1].trainingData.eventDAO.inputFile))
            assertThat(obj.testData.eventDAO.inputFile, equalTo(dss[i-1].testData.eventDAO.inputFile))
        }
    }

    @Test
    public void testUserSample() {
        cf.method = SplitMethods.sampleUsers(new RandomOrder<Rating>(),
                                                 new HoldoutNPartition<Rating>(5),
                                                 5);
        cf.execute()
        def dss = cf.dataSets
        assertThat(dss, hasSize(5))
        def allUsers = new LongOpenHashSet()
        for (ds in dss) {
            def train = ds.trainingData.eventDAO as TextEventDAO
            def test = ds.testData.eventDAO as TextEventDAO
            assertThat(train.inputFile.exists(), equalTo(true))
            assertThat(test.inputFile.exists(), equalTo(true))

            // test the users
            def users = ds.testData.userDAO.userIds
            allUsers += users
            // each test set should have 5 users
            assertThat(users, hasSize(5))
            // train data should have all users
            assertThat(ds.trainingData.userDAO.userIds, hasSize(100))
            // each test user should have 10 ratings
            def ued = ds.testData.userEventDAO
            for (user in users) {
                assertThat(ued.getEventsForUser(user), hasSize(5))
            }
        }
        assertThat(allUsers, hasSize(25))
        for (int i = 1; i <= 5; i++) {
            def train = tmp.root.toPath().resolve(String.format("part%02d.train.csv", i))
            assertThat(Files.exists(train), equalTo(true))
            def test = tmp.root.toPath().resolve(String.format("part%02d.test.csv", i))
            assertThat(Files.exists(test), equalTo(true))
            def specFile = tmp.root.toPath().resolve(String.format("part%02d.json", i))
            assertThat(Files.exists(specFile), equalTo(true))
            def spec = SpecUtils.load(TTDataSetSpec, specFile)
            def obj = DataSet.fromSpec(spec)
            assertThat(obj.trainingData, instanceOf(TextDataSource))
            assertThat(obj.testData, instanceOf(TextDataSource))
            assertThat(obj.queryData, nullValue())
            assertThat(obj.trainingData.eventDAO.inputFile, equalTo(dss[i-1].trainingData.eventDAO.inputFile))
            assertThat(obj.testData.eventDAO.inputFile, equalTo(dss[i-1].testData.eventDAO.inputFile))
        }
    }

    @Test
    public void testPartitionRatings() {
        cf.method = SplitMethods.partitionRatings()
        cf.execute()
        def dss = cf.dataSets
        assertThat(dss, hasSize(5))
        def allEvents = new HashSet<Event>();

        double perPart = ratings.size() / 5.0
        for (ds in dss) {
            def train = ds.trainingData.eventDAO as TextEventDAO
            def test = ds.testData.eventDAO as TextEventDAO
            assertThat(train.inputFile.exists(), equalTo(true))
            assertThat(test.inputFile.exists(), equalTo(true))

            // test the users
            def events = ObjectStreams.makeList ds.testData.eventDAO.streamEvents()
            allEvents += events;



            assertThat(events, hasSize(allOf(greaterThanOrEqualTo((Integer) Math.floor(perPart)),
                                             lessThanOrEqualTo((Integer) Math.ceil(perPart)))));

            // train data should have all the other ratings
            def tes = ObjectStreams.makeList ds.trainingData.eventDAO.streamEvents()
            assertThat(tes.size() + events.size(), equalTo(ratings.size()))
        }
        assertThat(allEvents, hasSize(ratings.size()))
        for (int i = 1; i <= 5; i++) {
            def train = tmp.root.toPath().resolve(String.format("part%02d.train.csv", i))
            assertThat(Files.exists(train), equalTo(true))
            def test = tmp.root.toPath().resolve(String.format("part%02d.test.csv", i))
            assertThat(Files.exists(test), equalTo(true))
            def specFile = tmp.root.toPath().resolve(String.format("part%02d.json", i))
            assertThat(Files.exists(specFile), equalTo(true))
            def spec = SpecUtils.load(TTDataSetSpec, specFile)
            def obj = DataSet.fromSpec(spec)
            assertThat(obj.trainingData, instanceOf(TextDataSource))
            assertThat(obj.testData, instanceOf(TextDataSource))
            assertThat(obj.queryData, nullValue())
            assertThat(obj.trainingData.eventDAO.inputFile, equalTo(dss[i - 1].trainingData.eventDAO.inputFile))
            assertThat(obj.testData.eventDAO.inputFile, equalTo(dss[i - 1].testData.eventDAO.inputFile))
        }
    }

    @Test
    public void testUserTimestampOrder() {
        cf.method = SplitMethods.partitionUsers(new TimestampOrder<Rating>(), new HoldoutNPartition<Rating>(5))
        cf.execute()
        def dss = cf.dataSets
        assertThat(dss, hasSize(5))
        def allUsers = new LongOpenHashSet()
        for (ds in dss) {
            def train = ds.trainingData.eventDAO as TextEventDAO
            def test = ds.testData.eventDAO as TextEventDAO
            assertThat(train.inputFile.exists(), equalTo(true))
            assertThat(test.inputFile.exists(), equalTo(true))

            // test the users
            def users = ds.testData.userDAO.userIds
            allUsers += users
            // each test set should have 20 users
            assertThat(users, hasSize(20))
            // train data should have all users
            assertThat(ds.trainingData.userDAO.userIds, hasSize(100))
            // each test user should have 10 ratings
            def ued = ds.testData.userEventDAO
            for (user in users) {
                def uevts = ued.getEventsForUser(user)
                def trainEvts = ds.trainingData.userEventDAO.getEventsForUser(user)
                def minTest = uevts*.timestamp.min()
                def maxTrain = trainEvts*.timestamp.max()
                assertThat(minTest, greaterThanOrEqualTo(maxTrain))
            }
        }
        assertThat(allUsers, hasSize(100))
    }

    @Test
    public void testRetainNPartition() {
        cf.method = SplitMethods.partitionUsers(new TimestampOrder<Rating>(), new RetainNPartition<Rating>(5));
        cf.execute()
        def dss = cf.dataSets
        assertThat(dss, hasSize(5))
        def allUsers = new LongOpenHashSet()
        for (ds in dss) {
            def train = ds.trainingData.eventDAO as TextEventDAO
            def test = ds.testData.eventDAO as TextEventDAO
            assertThat(train.inputFile.exists(), equalTo(true))
            assertThat(test.inputFile.exists(), equalTo(true))

            // test the users
            def users = ds.testData.userDAO.userIds
            allUsers += users
            // each test set should have 20 users
            assertThat(users, hasSize(20))
            // train data should have all users
            assertThat(ds.trainingData.userDAO.userIds, hasSize(100))
            // each test user should have 10 ratings
            def ued = ds.testData.userEventDAO
            for (user in users) {
                def uevts = ued.getEventsForUser(user)
                def trainEvts = ds.trainingData.userEventDAO.getEventsForUser(user)
                assertThat(trainEvts, hasSize(5));
                def minTest = uevts*.timestamp.min()
                def maxTrain = trainEvts*.timestamp.max()
                assertThat(minTest, greaterThanOrEqualTo(maxTrain))
            }
        }
        assertThat(allUsers, hasSize(100))
    }
}
