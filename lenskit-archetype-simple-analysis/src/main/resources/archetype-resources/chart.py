#!/usr/bin/env python

# This script generates a plot of the results.
# It requires Pandas, the Python data table library: http://pandas.pydata.org

import pandas as pd
import numpy as np
import matplotlib
matplotlib.use('svg')
import matplotlib.pyplot as plt

# Import data
frame = pd.read_csv('eval-results.csv')

# Compute means of metrics
means = frame.groupby('Algorithm').mean()

xvals = np.arange(len(means))

# Plot the RMSE
plots = plt.plot(xvals,
                 means['RMSE.ByUser'], 'ro',
                 means['RMSE.ByRating'], 'bs')
plt.legend(('By-User', 'Global'), loc='lower right', numpoints=1)
plt.xlim(-0.5, len(means) - 0.5)
plt.xticks(xvals, means.index)
plt.xlabel('Algorithm')
plt.ylabel('RMSE')
plt.savefig('rmse.svg')

# Plot the nDCG
plt.clf()
plots = plt.plot(xvals, means['nDCG'], 'ro')
plt.xlim(-0.5, len(means) - 0.5)
plt.xticks(xvals, means.index)
plt.xlabel('Algorithm')
plt.ylabel('nDCG')
plt.savefig('ndcg.svg')

# Plot the build and test time
plt.clf()
frame.boxplot('BuildTime', 'Algorithm')
plt.savefig('build-time.svg')

plt.clf()
frame.boxplot('TestTime', 'Algorithm')
plt.savefig('test-time.svg')
