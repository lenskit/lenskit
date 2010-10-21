package org.grouplens.reflens.util;

public abstract class AbstractProgressReporter implements ProgressReporter
{
	private long totalJobs = 0;

	public void setProgress(long current)
	{
		setProgress(current, Math.max(totalJobs, current));
	}

	public void setTotal(long total)
	{
		totalJobs = total;
	}
}