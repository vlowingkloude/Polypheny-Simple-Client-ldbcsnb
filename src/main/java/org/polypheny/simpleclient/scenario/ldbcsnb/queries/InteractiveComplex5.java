/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-8/3/24, 5:14 PM The Polypheny Project
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

public class InteractiveComplex5 extends QueryBuilder {
    int id = 0;
    final String cypher = """
            // Q5. New groups
            /*
            :params { personId: 6597069766734, minDate: 1288612800000 }
            */
            MATCH (person:Person { id: $personId })-[:KNOWS*1..2]-(otherPerson)
            WHERE
                person <> otherPerson
            WITH DISTINCT otherPerson
            MATCH (otherPerson)<-[membership:HAS_MEMBER]-(forum)
            WHERE
                membership.creationDate > $minDate
            WITH
                forum,
                collect(otherPerson) AS otherPersons
            OPTIONAL MATCH (otherPerson2)<-[:HAS_CREATOR]-(post)<-[:CONTAINER_OF]-(forum)
            WHERE
                otherPerson2 IN otherPersons
            WITH
                forum,
                count(post) AS postCount
            RETURN
                forum.title AS forumName,
                postCount
            ORDER BY
                postCount DESC,
                forum.id ASC
            LIMIT 20""";
    public InteractiveComplex5() {
        this.id = 0;
    }

    @Override
    public Query getNewQuery() {
        return null;
    }

    // used for warmup
    public Query getDefaultQuery() {
        return new GraphQuery( cypher.replace("$personId",  "6597069766734").replace("$minDate", "1288612800000") );
    }
}
