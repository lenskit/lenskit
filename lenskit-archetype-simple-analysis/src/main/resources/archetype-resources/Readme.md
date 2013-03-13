# LensKit-archetype-simple-analysis

This archetype is to build simple projects for doing analysis of
recommender algorithms.  

The key user files are:

* get-data.xml: an ant script that fetches one of the datasets from
  MovieLens, and unzips it.  Run automatically by Maven when needed.

* eval-simple.groovy: a groovy script that runs against the fetched
  dataset performing simple evaluations of some recommender
  algorithms.

* chart.R: an R script that runs against the results of the evaluation
  to produce some simple charts.

# More Information

Information on using the archetype is on the LensKit [wiki][] in the Getting Started section.

[wiki]: http://bitbucket.org/grouplens/lenskit/wiki/

