package org.grouplens.reflens.util;

public abstract interface ProgressReporterFactory
{
	public abstract ProgressReporter create(String label);
}