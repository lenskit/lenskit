package org.grouplens.reflens.bench;

import java.io.IOException;

import javax.annotation.Nullable;

import org.grouplens.reflens.data.AbstractProgressReporter;

import jline.Terminal;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class TerminalProgressReporter extends AbstractProgressReporter
{
	private int columnCount;
	private final String label;

	public TerminalProgressReporter()
	{
		this(null);
	}
	@Inject
	public TerminalProgressReporter(@Assisted @Nullable String label) {
		this.columnCount = -1;
		this.label = label;
		init();
	}

	private void init()
	{
		Terminal term = Terminal.getTerminal();
		this.columnCount = term.getTerminalWidth();
	}

	public void setProgress(long current, long total)
	{
		if (this.columnCount < 0) {
			return;
		}
		double fract = ((double) current) / total;
		String fstr;
		if (Math.round(fract * 1000.0) == 1000L)
			fstr = " 100%";
		else {
			fstr = String.format("%4.1f%%", fract * 100);
		}
		String tstr = Long.toString(total);
		int tw = tstr.length();

		int pbChars = this.columnCount - 11 - tw - tw;
		String lstr = "";
		if (this.label != null) {
			lstr = this.label + ": ";
		}
		pbChars -= lstr.length();
		if (pbChars < 5) {
			System.out.format("s%d/%d %s\r", new Object[] { lstr, Long.valueOf(current), Long.valueOf(total), fstr });
		} else {
			int nf = (int)(pbChars * fract);
			StringBuffer prog = new StringBuffer(pbChars);
			for (int i = 0; i < nf; i++)
				prog.append('=');
			for (int i = nf; i < pbChars; i++) {
				prog.append(' ');
			}
			System.out.format("%s[%s] %" + tw + "d/%d %s\r", new Object[] { lstr, prog.toString(), 
					Long.valueOf(current), Long.valueOf(total), fstr });
			System.out.flush();
		}
	}

	public void finish()
	{
		System.out.println();
		System.out.flush();
	}

	public static class EscapeException extends IOException
	{
		private static final long serialVersionUID = 1497686117319985433L;
		public EscapeException()
		{
		}

		public EscapeException(String msg)
		{
			super();
		}
		public EscapeException(Throwable what) {
			super();
		}
		public EscapeException(String msg, Throwable what) {
			super(what);
		}
	}
}