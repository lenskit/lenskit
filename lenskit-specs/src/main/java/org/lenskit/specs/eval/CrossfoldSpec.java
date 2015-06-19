package org.lenskit.specs.eval;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.lenskit.specs.AbstractSpec;
import org.lenskit.specs.data.DataSourceSpec;

/**
 * Specification for running a crossfold operation.
 */
public class CrossfoldSpec extends AbstractSpec {
    private String name;
    private DataSourceSpec source;

    private int partitionCount = 5;

    private CrossfoldMethod method;
    private PartitionMethodSpec userPartitionMethod;
    private Integer sampleSize;

    private boolean includeTimesamps = false;

    private OutputFormat outputFormat = OutputFormat.CSV;

    public CrossfoldSpec() {
        PartitionMethodSpec.Holdout hold = new PartitionMethodSpec.Holdout();
        hold.setOrder("random");
        hold.setCount(10);
        userPartitionMethod = hold;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataSourceSpec getSource() {
        return source;
    }

    public void setSource(DataSourceSpec source) {
        this.source = source;
    }

    public int getPartitionCount() {
        return partitionCount;
    }

    public void setPartitionCount(int partitionCount) {
        this.partitionCount = partitionCount;
    }

    public CrossfoldMethod getMethod() {
        return method;
    }

    public void setMethod(CrossfoldMethod method) {
        this.method = method;
    }

    public PartitionMethodSpec getUserPartitionMethod() {
        return userPartitionMethod;
    }

    public void setUserPartitionMethod(PartitionMethodSpec userPartitionMethod) {
        this.userPartitionMethod = userPartitionMethod;
    }

    public boolean getIncludeTimesamps() {
        return includeTimesamps;
    }

    public void setIncludeTimesamps(boolean includeTimesamps) {
        this.includeTimesamps = includeTimesamps;
    }

    public OutputFormat getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(OutputFormat outputFormat) {
        this.outputFormat = outputFormat;
    }

    public Integer getSampleSize() {
        return sampleSize;
    }

    @JsonProperty(required=false)
    public void setSampleSize(Integer sz) {
        sampleSize = sz;
    }
}
