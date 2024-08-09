/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-6/24/24, 3:57 PM The Polypheny Project
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

package org.polypheny.simpleclient.scenario.ldbcsnb.entities;

import org.polypheny.simpleclient.scenario.ldbcsnb.EntityHandler;
import org.polypheny.simpleclient.scenario.ldbcsnb.NodeEntity;

import java.util.List;

public class Comment extends NodeEntity {
    @Override
    public String getPath(String pathPrefix) {
        return pathPrefix + "/bi-sf1-composite-projected-fk/graphs/csv/bi/composite-projected-fk/initial_snapshot/dynamic/Comment/";
    }

    @Override
    public String getQuery(List<String> row) {
        // TODO: shall we use message_id here? OR use Comment_id?
        String baseQuery = "CREATE (comment_%s:Message:Comment {creationDate: \"%s\", id: %s, locationIP: \"%s\", browserUsed: \"%s\", content: \"%s\", length: %s})";
        return String.format(baseQuery, row.get(1), row.get(0), row.get(1), row.get(2), row.get(3), row.get(4), row.get(5));
    }

    @Override
    public String getBatchQuery(List<String> row) {
        String baseQuery = "(:Message:Comment {creationDate: \"%s\", id: %s, locationIP: \"%s\", browserUsed: \"%s\", content: \"%s\", length: %s})";
        return String.format(baseQuery, row.get(0), row.get(1), row.get(2), row.get(3), row.get(4), row.get(5));
    }
}
