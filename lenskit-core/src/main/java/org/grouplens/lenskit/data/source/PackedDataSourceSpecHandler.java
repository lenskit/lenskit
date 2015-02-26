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
package org.grouplens.lenskit.data.source;

import com.google.auto.service.AutoService;
import com.typesafe.config.Config;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.specs.SpecificationContext;
import org.grouplens.lenskit.specs.SpecificationException;

import java.net.MalformedURLException;
import java.net.URI;

/**
 * Configurator for packed sources.  Example configuration:
 *
 * <pre><code>
 *     type=pack
 *     file=ratings.pack
 *     domain {
 *         minimum = 0.5
 *         maximum = 1.0
 *         precision = 0.5
 *     }
 * </code></pre>
 */
@AutoService(DataSourceSpecHandler.class)
public class PackedDataSourceSpecHandler implements DataSourceSpecHandler {

    @Override
    public boolean handlesType(String type) {
        return type.equalsIgnoreCase("pack");
    }

    @Override
    public DataSource buildFromSpec(SpecificationContext ctx, Config cfg) throws SpecificationException {
        PackedDataSourceBuilder bld = new PackedDataSourceBuilder();

        if (cfg.hasPath("domain")) {
            bld.setDomain(ctx.build(PreferenceDomain.class, cfg.getConfig("domain")));
        }

        String file = cfg.getString("file");
        URI uri = ctx.getBaseURI().resolve(file);
        if (uri.isAbsolute()) {
            try {
                // FIXME This doesn't really work
                bld.setFile(uri.toURL().getFile());
            } catch (MalformedURLException e) {
                throw new SpecificationException(e);
            }
        } else {
            bld.setFile(uri.toString());
        }

        return bld.build();
    }
}
