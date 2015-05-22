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
