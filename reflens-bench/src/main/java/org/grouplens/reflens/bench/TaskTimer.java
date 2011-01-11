package org.grouplens.reflens.bench;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TaskTimer {
	private long startTime;
	private long stopTime;
	
	public TaskTimer() {
		start();
	}
	
	public void start() {
		startTime = System.currentTimeMillis();
		stopTime = -1;
	}
	
	public void stop() {
		stopTime = System.currentTimeMillis();
	}
	
	public long elapsedMillis() {
		long stop = stopTime;
		if (stop < 0)
			stop = System.currentTimeMillis();
		return stop - startTime;
	}
	
	public String elapsedPretty() {
		long elapsed = elapsedMillis();
		long secs = elapsed / 1000;
		long mins = secs / 60;
		long hrs = mins / 60;
		StringBuilder s = new StringBuilder();
		if (hrs > 0)
			s.append(String.format("%dh", hrs));
		if (mins > 0)
			s.append(String.format("%dm", mins % 60));
		s.append(String.format("%ds", secs % 60));
		return s.toString();
	}
}
