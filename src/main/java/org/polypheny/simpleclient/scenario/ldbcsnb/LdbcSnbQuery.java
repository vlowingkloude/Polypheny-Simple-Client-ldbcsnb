/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-8/7/24, 4:05 PM The Polypheny Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.polypheny.simpleclient.scenario.ldbcsnb;

import org.polypheny.simpleclient.query.QueryBuilder;

import java.io.InputStream;
import java.util.Scanner;
import org.polypheny.simpleclient.query.Query;

public abstract class LdbcSnbQuery extends QueryBuilder {
    // used to locate the next parameters
    protected int id = -1;
    protected final String cypher;

    public LdbcSnbQuery(String cypherFile) {
        InputStream is = ClassLoader.getSystemResourceAsStream("org/polypheny/simpleclient/scenario/ldbcsnb/" + cypherFile);
        assert is != null;
        Scanner scanner = new Scanner(is).useDelimiter("\\A");
        this.cypher = scanner.hasNext() ? scanner.next() : "";
    }

    // used for warmup
    public abstract Query getDefaultQuery();
}
