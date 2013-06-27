package org.grouplens.lenskit.eval.config

import org.apache.tools.ant.BuildException
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.nullValue
import static org.junit.Assert.*

/**
 * Teset that targets behave as expected.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class TestTargets extends ConfigTestBase {
    @Test
    void testBasicTarget() {
        def ranTarget = false
        def ranAction = false
        def script = evalScript {
            target("foobar") {
                ranTarget = true
                perform {
                    ranAction = true
                }
            }
        }
        assertThat(ranTarget, equalTo(true))
        assertThat(ranAction, equalTo(false))
        script.runTarget("foobar")
        assertThat(ranAction, equalTo(true))
    }

    @Test
    void testDeferTask() {
        def ranTarget = false
        def configuredTask = false
        def ranAction = false
        def script = evalScript {
            target("testing") {
                ranTarget = true
                mock {
                    configuredTask = true
                    action {
                        ranAction = true
                        "Hello, world!"
                    }
                }
            }
        }
        assertThat(ranTarget, equalTo(true))
        assertThat(configuredTask, equalTo(true))
        assertThat(ranAction, equalTo(false))
        script.runTarget("testing")
        assertThat(ranAction, equalTo(true))
    }

    @Test
    void testIgnoreOtherTasks() {
        def ranTarget = false
        def configuredTask = false
        def ranAction = false
        def ranIgnored = false
        def script = evalScript {
            target("ignored") {
                mock {
                    action {
                        ranIgnored = true
                    }
                }
            }
            target("testing") {
                ranTarget = true
                mock {
                    configuredTask = true
                    action {
                        ranAction = true
                    }
                }
            }
        }
        assertThat(ranTarget, equalTo(true))
        assertThat(configuredTask, equalTo(true))
        assertThat(ranAction, equalTo(false))
        script.runTarget("testing")
        assertThat(ranAction, equalTo(true))
        assertThat(ranIgnored, equalTo(false))
    }

    @Test
    void testRunDependentTarget() {
        def ranTarget = false
        def configuredTask = false
        def ranAction = false
        def ranRequired = false
        def script = evalScript {
            def req = target("required") {
                mock {
                    action {
                        ranRequired = true
                    }
                }
            }
            target("testing") {
                requires req
                ranTarget = true
                mock {
                    configuredTask = true
                    action {
                        assertThat(ranRequired, equalTo(true))
                        ranAction = true
                    }
                }
            }
        }
        assertThat(ranTarget, equalTo(true))
        assertThat(configuredTask, equalTo(true))
        assertThat(ranAction, equalTo(false))
        script.runTarget("testing")
        assertThat(ranAction, equalTo(true))
        assertThat(ranRequired, equalTo(true))
    }

    @Test
    void testRunByNameDependentTarget() {
        def ranTarget = false
        def configuredTask = false
        def ranAction = false
        def ranRequired = false
        def script = evalScript {
            target("required") {
                mock {
                    action {
                        ranRequired = true
                        "Goodbye, moon"
                    }
                }
            }
            target("testing") {
                requires "required"
                ranTarget = true
                mock {
                    configuredTask = true
                    action {
                        assertThat(ranRequired, equalTo(true))
                        ranAction = true
                        "Hello, world!"
                    }
                }
            }
        }
        assertThat(ranTarget, equalTo(true))
        assertThat(configuredTask, equalTo(true))
        assertThat(ranAction, equalTo(false))
        script.runTarget("testing")
        assertThat(ranAction, equalTo(true))
        assertThat(ranRequired, equalTo(true))
    }

    @Test
    void testSimpleAntTask() {
        def script = evalScript {
            target("ant") {
                ant.fail(message: "failure")
            }
        }
        try {
            script.runTarget("ant")
            fail("ant target should fail")
        } catch (BuildException e) {
            assertThat(e.getCause(), nullValue());
        }
    }
}
