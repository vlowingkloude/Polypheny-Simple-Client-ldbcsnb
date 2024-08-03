/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-8/3/24, 5:15 PM The Polypheny Project
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

public class InteractiveComplex6 extends QueryBuilder {
    int id = 0;
    final String cypher = """
            // Q6. Tag co-occurrence
            /*
            :params { personId: 4398046511333, tagName: "Carl_Gustaf_Emil_Mannerheim" }
            */
            MATCH (knownTag:Tag { name: $tagName })
            WITH knownTag.id as knownTagId
                        
            MATCH (person:Person { id: $personId })-[:KNOWS*1..2]-(friend)
            WHERE NOT person=friend
            WITH
                knownTagId,
                collect(distinct friend) as friends
            UNWIND friends as f
                MATCH (f)<-[:HAS_CREATOR]-(post:Post),
                      (post)-[:HAS_TAG]->(t:Tag{id: knownTagId}),
                      (post)-[:HAS_TAG]->(tag:Tag)
                WHERE NOT t = tag
                WITH
                    tag.name as tagName,
                    count(post) as postCount
            RETURN
                tagName,
                postCount
            ORDER BY
                postCount DESC,
                tagName ASC
            LIMIT 10""";
    public InteractiveComplex6() {
        this.id = 0;
    }

    @Override
    public Query getNewQuery() {
        return null;
    }
}
