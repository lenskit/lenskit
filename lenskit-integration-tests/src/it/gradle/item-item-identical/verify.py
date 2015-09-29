# LensKit, an open source recommender systems toolkit.
# Copyright 2010-2014 Regents of the University of Minnesota and contributors
# Work on LensKit has been funded by the National Science Foundation under
# grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
# details.
#
# You should have received a copy of the GNU General Public License along with
# this program; if not, write to the Free Software Foundation, Inc., 51
# Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

# Verification script to make sure that all 3 algorithms produce the same output.
# Uses Pandas.

import sys
try:
    import pandas as pd
except ImportError:
    print >>sys.stderr, "WARNING: pandas not installed. Skipping tests."
    sys.exit(0)

preds = pd.read_csv('predictions.csv')
algos = set(preds['Algorithm'])
preds_wide = preds.pivot_table(index=['User', 'Item', 'Rating'],
                               columns='Algorithm',
                               values='Prediction')
# FIXME Re-enable neighborhood checking
# nbrs_wide = preds.pivot_table(index=['User', 'Item', 'Rating'],
#                               columns='Algorithm',
#                               values='NbrCount')
pred_range = preds_wide.max(1) - preds_wide.min(1)

code = 0

# mismatched = nbrs_wide.Normalizing.fillna(0) != nbrs_wide.Standard.fillna(0)
# if mismatched.any():
#     nmm = mismatched.value_counts()[True]
#     print >>sys.stderr, "%d items have mismatched neighbor counts" % (nmm,)
#     print nbrs_wide[mismatched]
#     code |= 1

bad = preds_wide[pred_range >= 0.001]
if len(bad) > 0:
    print >>sys.stderr, "Have %d bad predictions (of %d)" % (len(bad),len(preds_wide))
    print >>sys.stderr, "Maximum error is %.3f" % (pred_range.max(),)
    print bad
    code |= 2

sys.exit(code)
