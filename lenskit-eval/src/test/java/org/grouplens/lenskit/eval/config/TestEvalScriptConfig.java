/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval.config;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

public class TestEvalScriptConfig {
    EvalScriptConfig esc;
    Properties props;

    @Before
    public void Initialize() {
	props = new Properties();
	props.setProperty("foo", "bar");
	props.setProperty("foo2", "bar");
	props.setProperty("foo3", "bar3");
	esc = new EvalScriptConfig(props);
    }

    /**
     * Make sure that changes to the Properties object don't change
     * the EvalScriptConfig after construction.
     */
    @Test
    public void TestClone() {
        assertEquals(esc.getProperty("foo"), "bar");
	props.setProperty("foo", "foobar");
        assertEquals(esc.getProperty("foo"), "bar");
    }

    @Test
    public void TestGet() {
        assertEquals(esc.getProperty("foo"), "bar");
        assertEquals(esc.getProperty("foo2"), "bar");
        assertEquals(esc.getProperty("foo3"), "bar3");
	assertEquals(esc.getProperty("foo4"), null);
	assertEquals(esc.getProperty("foo5", "bar5"), "bar5");
    }

    @Test
    public void TestSpecialNamesDefaults() {
        assertEquals(esc.getScriptDir(), ".");
        assertEquals(esc.getScriptName(), "eval.groovy");
        assertEquals(esc.getOutputDir(), ".");
        assertEquals(esc.getDataDir(), ".");
    }

    @Test
    public void TestSpecialNames() {
	props.setProperty("scriptDir", "../src/eval/");
	props.setProperty("dataDir", "../target/data/");
	props.setProperty("outputDir", "../target/analysis/");
	props.setProperty("scriptName", "my-eval.groovy");

	esc = new EvalScriptConfig(props);

        assertEquals(esc.getScriptDir(), "../src/eval/");
        assertEquals(esc.getDataDir(), "../target/data/");
        assertEquals(esc.getOutputDir(), "../target/analysis/");
        assertEquals(esc.getScriptName(), "my-eval.groovy");
    }

}
