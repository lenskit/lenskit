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
package org.lenskit.util.test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import javax.script.*;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

/**
 * Run a script using the script engine.
 */
public class RunScript {
    public static void main(String[] args) throws IOException, ScriptException {
        File scriptFile = new File(args[0]);
        ScriptEngineManager sem = new ScriptEngineManager();
        String ext = Files.getFileExtension(scriptFile.getName());
        ScriptEngine engine = sem.getEngineByExtension(ext);
        engine.put("cmdArgs", Arrays.asList(args).subList(1, args.length));
        try (Reader reader = Files.newReader(scriptFile, Charsets.UTF_8)) {
            engine.eval(reader);
        }
    }
}
