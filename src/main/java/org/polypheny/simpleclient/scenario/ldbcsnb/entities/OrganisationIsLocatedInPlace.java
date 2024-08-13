/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-7/4/24, 1:00 PM The Polypheny Project
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

public class OrganisationIsLocatedInPlace extends EdgeEntity {
    @Override
    public String getPath(String pathPrefix, int scaleFactor) {
        return pathPrefix + String.format("/bi-sf%d-composite-projected-fk/graphs/csv/bi/composite-projected-fk/initial_snapshot/static/Organisation_isLocatedIn_Place/", scaleFactor);
    }

    @Override
    public String getQuery(List<String> row) {
        String baseQuery = "MATCH (organisation_%s:Organisation {id: %s}), (place_%s:Place {id: %s}) CREATE (organisation_%s)-[:IS_LOCATED_IN]->(place_%s)";
        return String.format(baseQuery, row.get(0), row.get(0), row.get(1), row.get(1), row.get(0), row.get(1));
    }

    @Override
    public Map.Entry<String, String> getBatchQuery(List<String> row) {
        String matchClause = "(organisation_%s:Organisation {id: %s}), (place_%s:Place {id: %s})";
        String createClause = "(organisation_%s)-[:IS_LOCATED_IN]->(place_%s)";
        return Map.entry(String.format(matchClause, row.get(0), row.get(0), row.get(1), row.get(1)),
                String.format(createClause, row.get(0), row.get(1)));
    }
}
