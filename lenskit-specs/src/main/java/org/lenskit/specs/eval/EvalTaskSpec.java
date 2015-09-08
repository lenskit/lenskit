package org.lenskit.specs.eval;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.lenskit.specs.AbstractSpec;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Base type for eval task specifications.
 */
@JsonSubTypes({@JsonSubTypes.Type(value=PredictEvalTaskSpec.class, name="predict")})
@JsonTypeInfo(use= JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public abstract class EvalTaskSpec extends AbstractSpec {
    /**
     * Get the output files for this task.
     * @return The task's output files.
     */
    @JsonIgnore
    public abstract Set<Path> getOutputFiles();
}
