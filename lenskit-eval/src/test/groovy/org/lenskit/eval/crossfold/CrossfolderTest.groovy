package org.lenskit.eval.crossfold

import org.grouplens.lenskit.data.dao.EventCollectionDAO
import org.grouplens.lenskit.data.dao.EventDAO
import org.grouplens.lenskit.data.event.Rating
import org.grouplens.lenskit.data.source.DataSource
import org.grouplens.lenskit.data.source.GenericDataSource
import org.grouplens.lenskit.data.source.TextDataSource
import org.grouplens.lenskit.data.text.TextEventDAO
import org.grouplens.lenskit.eval.data.traintest.TTDataSet
import org.grouplens.lenskit.specs.SpecificationContext
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.lenskit.eval.OutputFormat

import java.nio.file.Files

import static net.java.quickcheck.generator.PrimitiveGenerators.doubles
import static net.java.quickcheck.generator.PrimitiveGeneratorsIterables.someLongs
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class CrossfolderTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder()

    private EventDAO sourceDAO
    private DataSource source
    private Crossfolder cf

    @Before
    public void createEvents() {
        def events = []
        for (user in someLongs()) {
            for (item in someLongs()) {
                double rating = doubles().next()
                events << Rating.create(user, item, rating)
            }
        }
        sourceDAO = EventCollectionDAO.create(events)
        source = new GenericDataSource("test", sourceDAO)
        cf = new Crossfolder()
        cf.source = source
        cf.setOutputDir(tmp.root)
    }

    @Test
    public void testFreshCFState() {
        assertThat(cf.name, equalTo("test"))
        assertThat(cf.partitionCount, equalTo(5))
        assertThat(cf.method, equalTo(CrossfoldMethod.PARTITION_USERS))
        assertThat(cf.skipIfUpToDate, equalTo(false))
        assertThat(cf.writeTimestamps, equalTo(true))
        assertThat(cf.outputFormat, equalTo(OutputFormat.CSV))
        def dss = cf.dataSets
        assertThat(dss, hasSize(5))
        for (ds in dss) {
            def dao = ds.trainingDAO as TextEventDAO
            assertThat(dao.inputFile.exists(), equalTo(false))
            assertThat(dao.inputFile.name, endsWith(".csv"))
        }
    }

    @Test
    public void testFreshCFRun() {
        cf.run()
        def dss = cf.dataSets
        assertThat(dss, hasSize(5))
        for (ds in dss) {
            def train = ds.trainingDAO as TextEventDAO
            def test = ds.testDAO as TextEventDAO
            assertThat(train.inputFile.exists(), equalTo(true))
            assertThat(test.inputFile.exists(), equalTo(true))
        }
        for (int i = 1; i <= 5; i++) {
            def train = tmp.root.toPath().resolve("train.${i}.csv")
            assertThat(Files.exists(train), equalTo(true))
            def test = tmp.root.toPath().resolve("test.${i}.csv")
            assertThat(Files.exists(test), equalTo(true))
            def spec = tmp.root.toPath().resolve("spec.${i}.json")
            assertThat(Files.exists(spec), equalTo(true))
            def specURI = spec.toUri()
            def obj = SpecificationContext.build(TTDataSet, specURI)
            assertThat(obj.trainingData, instanceOf(TextDataSource))
            assertThat(obj.testData, instanceOf(TextDataSource))
            assertThat(obj.queryData, nullValue())
            assertThat(obj.trainingDAO.inputFile, equalTo(dss[i-1].trainingDAO.inputFile))
            assertThat(obj.testDAO.inputFile, equalTo(dss[i-1].testDAO.inputFile))
        }
    }

    @Test
    public void test10PartCFRun() {
        cf.partitionCount = 10
        cf.run()
        def dss = cf.dataSets
        assertThat(dss, hasSize(10))
        for (ds in dss) {
            def train = ds.trainingDAO as TextEventDAO
            def test = ds.testDAO as TextEventDAO
            assertThat(train.inputFile.exists(), equalTo(true))
            assertThat(test.inputFile.exists(), equalTo(true))
        }
        for (int i = 1; i <= 10; i++) {
            def train = tmp.root.toPath().resolve("train.${i}.csv")
            assertThat(Files.exists(train), equalTo(true))
            def test = tmp.root.toPath().resolve("test.${i}.csv")
            assertThat(Files.exists(test), equalTo(true))
            def spec = tmp.root.toPath().resolve("spec.${i}.json")
            assertThat(Files.exists(spec), equalTo(true))
            def specURI = spec.toUri()
            def obj = SpecificationContext.build(TTDataSet, specURI)
            assertThat(obj.trainingData, instanceOf(TextDataSource))
            assertThat(obj.testData, instanceOf(TextDataSource))
            assertThat(obj.queryData, nullValue())
            assertThat(obj.trainingDAO.inputFile, equalTo(dss[i-1].trainingDAO.inputFile))
            assertThat(obj.testDAO.inputFile, equalTo(dss[i-1].testDAO.inputFile))
        }
    }
}
