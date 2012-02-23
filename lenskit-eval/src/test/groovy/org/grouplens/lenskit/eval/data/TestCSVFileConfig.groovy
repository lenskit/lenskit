package org.grouplens.lenskit.eval.data

import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import org.junit.Test

import org.grouplens.lenskit.eval.config.ConfigTestBase

/**
 * @author Michael Ekstrand
 */
class TestCSVFileConfig extends ConfigTestBase {
    @Test
    void testBuilderAvailable() {
        def factories = engine.getFactories()
        assertThat(factories.get("csvfile"), notNullValue())
    }
    @Test
    void testBasic() {
        def source = eval {
            csvfile("ml-100k/u.data")
        }
        assertThat(source, notNullValue())
        assertThat(source.name, equalTo("ml-100k/u.data"))
        assertThat(source.file, equalTo(new File("ml-100k/u.data")))
        assertThat(source.delimiter, equalTo("\t"))
    }

    @Test
    void testFileDelim() {
        def source = eval {
            csvfile("ml-100k") {
                file "ml-100k/u.data"
                delimiter "::"
            }
        }
        assertThat(source.name, equalTo("ml-100k"))
        assertThat(source.file, equalTo(new File("ml-100k/u.data")))
        assertThat(source.delimiter, equalTo("::"))
    }

    @Test
    void testFileNoName() {
        def source = eval {
            csvfile {
                file "ml-100k/u.data"
            }
        }
        assertThat(source.name, equalTo("ml-100k/u.data"))
        assertThat(source.file, equalTo(new File("ml-100k/u.data")))
    }
}
