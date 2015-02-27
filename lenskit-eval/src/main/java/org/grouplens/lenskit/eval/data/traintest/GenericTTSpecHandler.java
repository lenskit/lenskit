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
package org.grouplens.lenskit.eval.data.traintest;

import com.typesafe.config.Config;
import org.grouplens.lenskit.data.source.DataSource;
import org.grouplens.lenskit.specs.SpecHandler;
import org.grouplens.lenskit.specs.SpecificationContext;
import org.grouplens.lenskit.specs.SpecificationException;

import java.util.UUID;

public class GenericTTSpecHandler implements SpecHandler<TTDataSet> {
    @Override
    public boolean handlesType(String type) {
        return "train-test".equals(type);
    }

    @Override
    public TTDataSet buildFromSpec(SpecificationContext context, Config cfg) throws SpecificationException {
        GenericTTDataBuilder bld = new GenericTTDataBuilder();
        bld.setTrain(context.build(DataSource.class, cfg.getConfig("train")))
           .setTest(context.build(DataSource.class, cfg.getConfig("test")));
        if (cfg.hasPath("query")) {
            bld.setQuery(context.build(DataSource.class, cfg.getConfig("query")));
        }
        if (cfg.hasPath("group")) {
            bld.setIsolationGroup(UUID.fromString(cfg.getString("group")));
        }
        if (cfg.hasPath("name")) {
            bld.setName(cfg.getString("name"));
        }
        return bld.build();
    }
}
