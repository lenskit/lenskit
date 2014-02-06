# LensKit-archetype-simple-analysis

This archetype is to build simple projects for doing analysis of
recommender algorithms.  

The key user files are:

* eval-simple.groovy: a groovy script that runs against the fetched
  dataset performing simple evaluations of some recommender
  algorithms. It also downloads the ML-100K data set and runs R to
  produce charts of the algorithm performance.

* chart.py: a Python script that runs against the results of the
  evaluation to produce some simple charts.  This requires matplotlib.

# More Information

Information on using the archetype is on the LensKit [wiki][] in the Getting Started section.

[wiki]: http://bitbucket.org/grouplens/lenskit/wiki/

