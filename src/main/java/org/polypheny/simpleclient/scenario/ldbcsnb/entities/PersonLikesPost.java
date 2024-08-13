/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-7/4/24, 5:23 PM The Polypheny Project
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

import org.polypheny.simpleclient.scenario.ldbcsnb.EdgeEntity;

import java.util.List;
import java.util.Map;

public class PersonLikesPost extends EdgeEntity {
    @Override
    public String getPath(String pathPrefix, int scaleFactor) {
        return pathPrefix + String.format("/bi-sf%d-composite-projected-fk/graphs/csv/bi/composite-projected-fk/initial_snapshot/dynamic/Person_likes_Post/", scaleFactor);
    }

    @Override
    public String getQuery(List<String> row) {
        String baseQuery = "MATCH (person_%s:Person {id: %s}), (post_%s:Post {id: %s}) CREATE (person_%s)-[:LIKES {creationDate: DATETIME(\"%s\")}]->(post_%s)";
        return String.format(baseQuery, row.get(1), row.get(1), row.get(2), row.get(2), row.get(1), row.get(0), row.get(2));
    }

    @Override
    public Map.Entry<String, String> getBatchQuery(List<String> row) {
        String matchClause = "(person_%s:Person {id: %s}), (post_%s:Post {id: %s})";
        String createClause = "(person_%s)-[:LIKES {creationDate: DATETIME(\"%s\")}]->(post_%s)";
        return Map.entry(String.format(matchClause, row.get(1), row.get(1), row.get(2), row.get(2)),
                String.format(createClause, row.get(1), row.get(0), row.get(2)));
    }
}
