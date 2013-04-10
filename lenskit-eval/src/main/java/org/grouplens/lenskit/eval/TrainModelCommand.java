package org.grouplens.lenskit.eval;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.io.Closer;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.eval.algorithm.LenskitAlgorithmInstance;
import org.grouplens.lenskit.eval.data.DataSource;
import org.grouplens.lenskit.util.LogContext;

import java.io.File;
import java.io.IOException;

/**
 * Train a recommender algorithm and process it with a function.
 */
public class TrainModelCommand<T> extends AbstractCommand<T> {

    private LenskitAlgorithmInstance algorithm;
    private File writeFile;
    private DataSource inputData;
    private Function<LenskitRecommender, T> action;

    public TrainModelCommand() {
        super("train-model");
    }

    public TrainModelCommand(String name) {
        super(name);
    }

    /**
     * Configure the algorithm.
     * @param algo The algorithm to configure.
     * @return The command (for chaining).
     */
    public TrainModelCommand setAlgorithm(LenskitAlgorithmInstance algo) {
        algorithm = algo;
        return this;
    }

    /**
     * Specify a file to write. The trained recommender algorithm will be written
     * to this file.
     * @param file The file name.
     * @return The command (for chaining).
     */
    public TrainModelCommand setWriteFile(File file) {
        writeFile = file;
        return this;
    }

    /**
     * Specify the data source to train on.
     * @param data The input data source.
     * @return The builder (for chaining).
     */
    public TrainModelCommand setInput(DataSource data) {
        inputData = data;
        return this;
    }

    /**
     * Set the action to invoke.  The action's return value will be returned
     * from {@link #call()}.
     * @param act The action to invoke.
     * @return The command (for chaining).
     */
    public TrainModelCommand setAction(Function<LenskitRecommender,T> act) {
        action = act;
        return this;
    }

    @Override
    public T call() throws CommandException {
        Preconditions.checkState(algorithm != null, "no algorithm specified");
        Preconditions.checkState(inputData != null, "no input data specified");
        Preconditions.checkState(inputData != null, "no action specified");
        LogContext context = new LogContext();
        try {
            context.put("lenskit.eval.command.class", getName());
            context.put("lenskit.eval.command.name", getName());
            Closer closer = Closer.create();
            try {
                DAOFactory daoFactory = inputData.getDAOFactory();
                DataAccessObject dao = closer.register(daoFactory.snapshot());
                // TODO Support serializing the recommender
                LenskitRecommender rec;
                try {
                    rec = closer.register(algorithm.buildRecommender(
                            dao, null, inputData.getPreferenceDomain(), false));
                } catch (RecommenderBuildException e) {
                    throw new CommandException(getName() + ": error building recommender", e);
                }
                return action.apply(rec);
            } catch (Throwable th) {
                throw closer.rethrow(th, CommandException.class);
            } finally {
                closer.close();
            }
        } catch (IOException ioe) {
            throw new CommandException("error in " + getName(), ioe);
        } finally {
            context.finish();
        }
    }
}
