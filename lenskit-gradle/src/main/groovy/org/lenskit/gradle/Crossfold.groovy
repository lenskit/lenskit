package org.lenskit.gradle

import groovy.json.JsonBuilder
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory

/**
 * Crossfold a data set.  This task can only crossfold a single data set; multiple tasks must be used to produce
 * multiple cross-validation splits.
 */
class Crossfold extends LenskitTask {
    /**
     * The input data specification.
     */
    private def inputSpec

    /**
     * The output directory for cross-validation.  Defaults to "build/$name.out", where $name is the name of the task.
     */
    def outputDir

    public Crossfold() {
        conventionMapping.outputDir = {
            "$project.buildDir/${name}.out"
        }
    }

    /**
     * Configure the input data.  This should be an input specification in Groovy {@link JsonBuilder} syntax.  For
     * example:
     *
     * ```
     * input {
     *     type "text"
     *     file "ratings.csv"
     *     delimiter ","
     *     domain {
     *         minimum 1.0
     *         maximum 5.0
     *         precision 1.0
     *     }
     * }
     * ```
     *
     * @param input The closure expressing the input specification.
     */
    void input(Closure input) {
        def dlg = new JsonBuilder()
        dlg {
            // TODO Support _base_uri in spec handlers
            _base_uri project.rootDir.toURI()
        }
        dlg.call(input)
        inputSpec = dlg.content
    }

    /**
     * Configure an input CSV file of ratings.  Convenience method; {@link #input(Closure)} is more general.
     * @param csv A CSV file containing ratings.
     */
    void inputFile(File csv) {
        input {
            type "csv"
            file csv.toString()
        }
    }

    @InputFile
    File guessInputFile() {
        def map = inputSpec as Map
        def f = map?.get('file')
        if (f != null) {
            project.file(f)
        } else {
            null
        }
    }

    @OutputDirectory
    File getOutputDirectory() {
        return project.file(getOutputDir())
    }

    @Override
    String getCommand() {
        return 'crossfold'
    }

    @Override
    List getCommandArgs() {
        def args = []
        args << '-o'
        args << getOutputDirectory()
    }
}
