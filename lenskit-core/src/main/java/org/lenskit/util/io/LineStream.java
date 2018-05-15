/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.util.io;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.io.Files;
import org.apache.commons.text.StringTokenizer;
import org.lenskit.data.dao.DataAccessException;

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
            throw new DataAccessException("error reading line", e);
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

    public ObjectStream<List<String>> tokenize(StringTokenizer tok) {
        return ObjectStreams.transform(this, new TokenizerFunction(tok));
    }

    private static class TokenizerFunction implements Function<String,List<String>> {
        StringTokenizer tokenizer;

        public TokenizerFunction(StringTokenizer tok) {
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
