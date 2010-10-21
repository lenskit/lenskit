package org.grouplens.reflens.util.parallel;

public interface ObjectWorker<T> extends Worker {
	public void doJob(T object);
}
