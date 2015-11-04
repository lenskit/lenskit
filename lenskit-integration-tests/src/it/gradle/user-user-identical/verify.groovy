/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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


import javax.script.ScriptEngineManager
import org.renjin.eval.EvalException

import static org.hamcrest.MatcherAssert.assertThat

File resultsFile = new File("results.csv")
File predictFile = new File("predictions.csv")

assertThat("output file exists", resultsFile.exists())
assertThat("predict file exists", predictFile.exists())

def sem = new ScriptEngineManager()
def engine = sem.getEngineByName("Renjin")
assert engine != null

def script = new File("verify.R")
try {
    script.withReader { rdr ->
        engine.eval(rdr)
    }
} catch (EvalException ex) {
    System.err.println("verification failed: " + ex.message)
    System.exit(2)
}

