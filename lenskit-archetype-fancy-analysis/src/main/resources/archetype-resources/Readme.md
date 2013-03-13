# LensKit-archetype-fancy-analysis

This archetype is to build more sophisticated projects for doing
analysis of recommender algorithms, and that organize the input and
output according to maven best practices.  All of the scripts for the
eval are in the src/eval directory.  Each script takes in some input
data, and produces some output data.  The archetype is intended to be
used with the following structure:

* lenskit-pre-eval: gets data into the target/data directory.  The 
  ant script in src/eval/get-data.xml is used to fetch the data.

* lenskit-eval: takes data from the target/data directory and creates
  a set of crossfold datasets also in target/data.  Runs an evaluation
  script in src/eval/eval.groovy, which operates on the crossfold data
  in target/data, and produces output in target/analysis.

* lenskit-analysis: takes data in the csv files in the target-analysis
  directory and processes them using an R script that generates
  statistical analysis of the performance of the algorithms.  Requires
  that R be installed on the computer running maven.

The key user files that you are likely to want to edit are:
* pom.xml: to change the value of grouplens.mldata.acknowledgement, 
  or to change the dataset that is downloaded.
* src/eval/get-data.xml: to change the dataset that is downloaded.  
  May require changes in pom.xml as well.
* src/eval/eval.groovy: to change the lenskit evaluation that is run, 
  perhaps by configuring different recommenders.
* src/eval/chart.R: to change the analysis of the output data in target/analysis,  
  perhaps including the charts that are generated.

This structure fits the Maven model: all input files are in the src
tree, and all generated files are in the target tree, where they may
be cleaned by the clean target.

## Example Predictor

There is a simple example predictor in src/main/java.  This predictor
includes a model that generates item means, and a predictor based on
that model.  You may find the model and predictor useful as starting
points for your own predictors.  The analysis script uses this
predictor along with some well-known prediction algorithms.

# More Information

Information on using the archetype is on the LensKit [wiki][] in the Getting Started section.

[wiki]: http://bitbucket.org/grouplens/lenskit/wiki/
