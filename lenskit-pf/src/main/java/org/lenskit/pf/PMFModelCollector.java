package org.lenskit.pf;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;


public class PMFModelCollector implements Collector<PMFModel.ModelEntry, PMFModel, PMFModel> {
    @Override
    public Supplier<PMFModel> supplier() {
        return PMFModel::new;
    }

    @Override
    public BiConsumer<PMFModel, PMFModel.ModelEntry> accumulator() {
        return (PMFModel::addEntry);
    }

    @Override
    public BinaryOperator<PMFModel> combiner() {
        return (PMFModel::addAll);
    }

    @Override
    public Function<PMFModel, PMFModel> finisher() {
        return Function.identity();
    }

    @Override
    public Set<Characteristics> characteristics() {
        return EnumSet.of(Characteristics.UNORDERED, Characteristics.IDENTITY_FINISH);
    }
}
