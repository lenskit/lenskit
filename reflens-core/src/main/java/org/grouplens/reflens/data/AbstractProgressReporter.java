package org.grouplens.reflens.data;

import org.grouplens.reflens.util.ProgressReporter;

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