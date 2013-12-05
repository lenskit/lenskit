# LensKit-archetype-fancy-analysis

This archetype is to build more sophisticated projects for doing
analysis of recommender algorithms, and that organize the input and
output according to maven best practices.  All of the scripts for the
eval are in the src/eval directory.  Each script takes in some input
data, and produces some output data.  The archetype is intended to be
used with the following structure:

* lenskit-eval: takes data from the target/data directory and creates
  a set of crossfold datasets also in target/data.  Runs an evaluation
  script in src/eval/eval.groovy, which operates on the crossfold data
  in target/data, and produces output in target/analysis.

The key user files that you are likely to want to edit are:
* pom.xml: to change the value of grouplens.mldata.acknowledgement and
  do general configuration.
* src/eval/eval.groovy: to change the lenskit evaluation that is run,
  perhaps by configuring different recommenders.  This also downloads the
  data set and runs the R script for post-analysis.
* src/eval/chart.py: to change the analysis of the output data in target/analysis,
  perhaps including the charts that are generated.

This structure fits the Maven model: all input files are in the src
tree, and all generated files are in the target tree, where they may
be cleaned by the clean target.

## Example Scorer

There is a simple example scorer in src/main/java.  This scorer
includes a model that generates item mean ratings, and a scorer based on
that model.  You may find the model and predictor useful as starting
points for your own predictors.  The analysis script uses this
scorer along with some well-known rating prediction algorithms.

# More Information

Information on using the archetype is on the LensKit [wiki][] in the Getting Started section.

[wiki]: http://bitbucket.org/grouplens/lenskit/wiki/
