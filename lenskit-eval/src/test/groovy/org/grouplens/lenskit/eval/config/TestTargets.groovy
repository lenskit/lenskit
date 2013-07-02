/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval.config

import org.apache.tools.ant.BuildException
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.nullValue
import static org.junit.Assert.*

/**
 * Test that targets behave as expected.
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
        script.project.executeTarget("foobar")
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
        script.project.executeTarget("testing")
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
        script.project.executeTarget("testing")
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
        script.project.executeTarget("testing")
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
        script.project.executeTarget("testing")
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
            script.project.executeTarget("ant")
            fail("ant target should fail")
        } catch (BuildException e) {
            assertThat(e.getCause(), nullValue());
        }
    }
}
