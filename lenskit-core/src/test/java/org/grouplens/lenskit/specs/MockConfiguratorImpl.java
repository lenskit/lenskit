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
package org.grouplens.lenskit.specs;

import com.google.auto.service.AutoService;
import com.google.common.collect.Maps;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;

import java.util.Map;

/**
 * Basic implementation of the test configurator.
 */
@AutoService(MockConfigurator.class)
public class MockConfiguratorImpl implements MockConfigurator {
    @Override
    public boolean handlesType(String type) {
        return type.equals("hash");
    }

    @Override
    public Map<String, String> buildFromSpec(SpecificationContext ctx, Config cfg) throws SpecificationException {
        Map<String,String> map = Maps.newHashMap();
        for (Map.Entry<String,ConfigValue> entry: cfg.entrySet()) {
            if (!entry.getKey().equals("type")) {
                map.put(entry.getKey(), entry.getValue().unwrapped().toString());
            }
        }
        return map;
    }
}
