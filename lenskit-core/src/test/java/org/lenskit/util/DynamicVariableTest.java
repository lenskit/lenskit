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
package org.lenskit.util;

import net.java.quickcheck.generator.CombinedGenerators;
import net.java.quickcheck.generator.PrimitiveGenerators;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class DynamicVariableTest {
    @Test
    public void testEmpty() {
        DynamicVariable<String> var = new DynamicVariable<>();
        assertThat(var.get(), nullValue());
    }

    @Test
    public void testInitial() {
        DynamicVariable<String> var = new DynamicVariable<>("foo");
        assertThat(var.get(), equalTo("foo"));
    }

    @Test
    public void testAssign() {
        DynamicVariable<String> var = new DynamicVariable<>("bar");
        DynamicVariable.Scope scope = var.assign("wombat");
        assertThat(scope, notNullValue());
        assertThat(var.get(), equalTo("wombat"));
        scope.close();
        assertThat(var.get(), equalTo("bar"));
    }

    @Test
    public void testManyAssignments() {
        for (int i = 0; i < 10; i++) {
            List<String> strings = CombinedGenerators.nonEmptyLists(PrimitiveGenerators.strings()).next();
            DynamicVariable<String> var = new DynamicVariable<>();
            List<DynamicVariable.Scope> scopes = new ArrayList<>(strings.size());
            for (String str: strings) {
                if (scopes.size() > 0) {
                    assertThat(var.get(), equalTo(strings.get(scopes.size() - 1)));
                }
                scopes.add(var.assign(str));
                assertThat(var.get(), equalTo(str));
            }

            for (int j = scopes.size() - 1; j >= 0; j--) {
                assertThat(var.get(), equalTo(strings.get(j)));
                scopes.get(j).close();
            }
            assertThat(var.get(), nullValue());
        }
    }
}
