package org.grouplens.reflens.util;

public abstract interface ProgressReporter
{
	public abstract void setTotal(long total);
	public abstract void setProgress(long current);
	public abstract void setProgress(long current, long total);
	public abstract void finish();
}