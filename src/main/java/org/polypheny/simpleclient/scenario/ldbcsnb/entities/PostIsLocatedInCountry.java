/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-7/4/24, 5:33 PM The Polypheny Project
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
import org.polypheny.simpleclient.scenario.ldbcsnb.EntityHandler;

import java.util.List;
import java.util.Map;

public class PostIsLocatedInCountry extends EdgeEntity {
    @Override
    public String getPath(String pathPrefix) {
        return pathPrefix + "/bi-sf1-composite-projected-fk/graphs/csv/bi/composite-projected-fk/initial_snapshot/dynamic/Post_isLocatedIn_Country/";
    }

    @Override
    public String getQuery(List<String> row) {
        // TODO: shall we use Country label here?
        String baseQuery = "MATCH (post_%s:Post {id: %s}), (place_%s:Place {id: %s}) CREATE (post_%s)-[:IS_LOCATED_IN {creationDate: DATETIME(\"%s\")}]->(place_%s)";
        return String.format(baseQuery, row.get(1), row.get(1), row.get(2), row.get(2), row.get(1), row.get(0), row.get(2));
    }

    @Override
    public Map.Entry<String, String> getBatchQuery(List<String> row) {
        String matchClause = "(post_%s:Post {id: %s}), (place_%s:Place {id: %s})";
        String createClause = "(post_%s)-[:IS_LOCATED_IN {creationDate: DATETIME(\"%s\")}]->(place_%s)";
        return Map.entry(String.format(matchClause, row.get(1), row.get(1), row.get(2), row.get(2)),
                String.format(createClause, row.get(1), row.get(0), row.get(2)));
    }
}
