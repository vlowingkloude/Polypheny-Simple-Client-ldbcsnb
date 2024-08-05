/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-8/3/24, 5:12 PM The Polypheny Project
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

package org.polypheny.simpleclient.scenario.ldbcsnb.queries;

import org.polypheny.simpleclient.query.Query;
import org.polypheny.simpleclient.query.QueryBuilder;
import org.polypheny.simpleclient.scenario.graph.GraphQuery;

public class InteractiveComplex4 extends QueryBuilder {
    int id = 0;
    final String cypher = """
            // Q4. New topics
            /*
            :params { personId: 4398046511333, startDate: 1275350400000, endDate: 1277856000000 }
            */
            MATCH (person:Person {id: $personId })-[:KNOWS]-(friend:Person),
                  (friend)<-[:HAS_CREATOR]-(post:Post)-[:HAS_TAG]->(tag)
            WITH DISTINCT tag, post
            WITH tag,
                 CASE
                   WHEN $endDate > post.creationDate >= $startDate THEN 1
                   ELSE 0
                 END AS valid,
                 CASE
                   WHEN $startDate > post.creationDate THEN 1
                   ELSE 0
                 END AS inValid
            WITH tag, sum(valid) AS postCount, sum(inValid) AS inValidPostCount
            WHERE postCount>0 AND inValidPostCount=0
            RETURN tag.name AS tagName, postCount
            ORDER BY postCount DESC, tagName ASC
            LIMIT 10""";
    public InteractiveComplex4() {
        this.id = 0;
    }

    @Override
    public Query getNewQuery() {
        return null;
    }

    // used for warmup
    public Query getDefaultQuery() {
        return new GraphQuery( cypher.replace("$personId",  "4398046511333").replace("$startDate", "1275350400000").replace("$endDate", "1277856000000") );
    }
}
