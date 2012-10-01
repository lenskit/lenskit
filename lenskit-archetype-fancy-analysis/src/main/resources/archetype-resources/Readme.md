* Be careful editing this pom, because its variables are substituted
  at two different times.  Variables like ${project.version} are
  substituted at the time the archetype is run by a user to create a
  project.  Variables with backslashes in front of them like
  \${version} are left as variables by the archetype, so they can be
  substituted at project build time.


