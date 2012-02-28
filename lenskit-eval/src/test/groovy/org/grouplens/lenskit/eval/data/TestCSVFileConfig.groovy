package org.grouplens.lenskit.eval.data

import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import org.junit.Test

import org.grouplens.lenskit.eval.config.ConfigTestBase
import org.grouplens.lenskit.data.pref.PreferenceDomain

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

    @Test
    void testDomain() {
        def source = eval {
            csvfile("ml-100k") {
                domain {
                    minimum 1.0
                    maximum 5.0
                    precision 1.0
                }
            }
        }
        assertThat(source.domain, equalTo(new PreferenceDomain(1.0, 5.0, 1.0)))
    }

    /**
     * Can we use {@link GString}s?
     */
    @Test
    void testGString() {
        def name = "ml-100k"
        def source = eval {
            csvfile(name) {
                file "${name}.csv"
            }
        }
        assertThat(source.name, equalTo("ml-100k"))
        assertThat(source.file, equalTo(new File("ml-100k.csv")))
    }
}
