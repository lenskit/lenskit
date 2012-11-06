# LensKit-archetype-fancy-analysis

This archetype is to build more sophisticated projects for doing
analysis of recommender algorithms.  Detailed information about
using this archetype is in the src/main/resources/archetype-resources/Readme.md
file, which is also installed each type a user uses this archetype to generate
a project.  

* Be careful editing the pom in src/main/resources/archetype-resources, 
  because its variables are substituted
  at two different times.  Variables like ${project.version} are
  substituted at the time the archetype is run by a user to create a
  project.  Variables with backslashes in front of them like
  \${version} are left as variables by the archetype, so they can be
  substituted at project build time.


