/*
 * RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010 Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but
 * you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

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
			System.out.format("%s%d/%d %s\r", lstr, Long.valueOf(current), Long.valueOf(total), fstr );
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