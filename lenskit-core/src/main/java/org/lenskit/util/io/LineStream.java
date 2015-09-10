/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.lenskit.util.io;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.io.Files;
import org.apache.commons.lang3.text.StrTokenizer;
import org.grouplens.lenskit.util.io.CompressionMode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillCloseWhenClosed;
import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Stream that reads lines from a file.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.10
 */
public class LineStream extends AbstractObjectStream<String> {
    private BufferedReader input;
    private int lineNumber = 0;

    /**
     * Construct a stream reading lines from a buffered reader.
     *
     * @param in    The input reader.
     */
    public LineStream(@WillCloseWhenClosed @Nonnull BufferedReader in) {
        input = in;
    }

    /**
     * Open a delimited text stream as a file.
     *
     * @param file The file to open.
     * @return The stream.
     * @throws FileNotFoundException if there is an error opening the file.
     */
    public static LineStream openFile(File file) throws FileNotFoundException {
        // REVIEW do we want to use the default charset?
        return new LineStream(Files.newReader(file, Charset.defaultCharset()));
    }

    /**
     * Open a delimited text stream as a file.
     *
     * @param file The file to open.
     * @return The stream.
     * @throws FileNotFoundException if there is an error opening the file.
     */
    public static LineStream openFile(File file, CompressionMode comp) throws IOException {
        FileInputStream fin = new FileInputStream(file);
        InputStream rawin = comp.getEffectiveCompressionMode(file.getName()).wrapInput(fin);
        // REVIEW do we want to use the default charset?
        Reader reader = new InputStreamReader(rawin, Charset.defaultCharset());
        BufferedReader buffer = new BufferedReader(reader);
        return new LineStream(buffer);
    }

    @Override
    @Nullable
    public String readObject() {
        try {
            String line = input.readLine();
            lineNumber++;
            return line;
        } catch (IOException e) {
            throw new RuntimeException("error reading line", e);
        }
    }

    /**
     * Return the number of the line returned by the last call to {@link #readObject()}.
     *
     * @return The number of the last line retrieved.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public void close() {
        try {
            input.close();
        } catch (IOException ex) {
            throw Throwables.propagate(ex);
        }
    }

    public ObjectStream<List<String>> tokenize(StrTokenizer tok) {
        return ObjectStreams.transform(this, new TokenizerFunction(tok));
    }

    private static class TokenizerFunction implements Function<String,List<String>> {
        StrTokenizer tokenizer;

        public TokenizerFunction(StrTokenizer tok) {
            tokenizer = tok;
        }

        @Nullable
        @Override
        public List<String> apply(@Nullable String input) {
            if (input == null) {
                return null;
            }
            tokenizer.reset(input);
            return tokenizer.getTokenList();
        }
    }
}
