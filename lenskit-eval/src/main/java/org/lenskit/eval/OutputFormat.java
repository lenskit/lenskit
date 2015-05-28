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
package org.lenskit.eval;

import org.grouplens.lenskit.util.io.CompressionMode;

public enum OutputFormat {
    CSV {
        @Override
        public String getSuffix() {
            return "csv";
        }

        @Override
        public CompressionMode getCompressionMode() {
            return CompressionMode.NONE;
        }
    },
    CSV_GZIP {
        @Override
        public String getSuffix() {
            return "csv.gz";
        }

        @Override
        public CompressionMode getCompressionMode() {
            return CompressionMode.GZIP;
        }
    },
    CSV_XZ {
        @Override
        public String getSuffix() {
            return "csv.xz";
        }

        @Override
        public CompressionMode getCompressionMode() {
            return CompressionMode.XZ;
        }
    },
    PACK {
        @Override
        public String getSuffix() {
            return "pack";
        }

        @Override
        public CompressionMode getCompressionMode() {
            return null;
        }
    };

    public abstract String getSuffix();
    public abstract CompressionMode getCompressionMode();
}
