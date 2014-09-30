# Check whether we can run with Python dependencies

import sys
try:
    import pandas
except ImportError:
    print >>sys.stderr, "Module `pandas' not installed"
    sys.exit(1)
