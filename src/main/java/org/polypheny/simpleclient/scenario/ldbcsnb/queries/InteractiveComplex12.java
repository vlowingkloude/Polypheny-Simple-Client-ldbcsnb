/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-8/3/24, 5:22 PM The Polypheny Project
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

public class InteractiveComplex12 extends QueryBuilder {
    int id;
    final String cypher = """
            // Q12. Expert search
            /*
            :params { personId: 10995116278009, tagClassName: "Monarch" }
            */
            MATCH (tag:Tag)-[:HAS_TYPE|IS_SUBCLASS_OF*0..]->(baseTagClass:TagClass)
            WHERE tag.name = $tagClassName OR baseTagClass.name = $tagClassName
            WITH collect(tag.id) as tags
            MATCH (:Person {id: $personId })-[:KNOWS]-(friend:Person)<-[:HAS_CREATOR]-(comment:Comment)-[:REPLY_OF]->(:Post)-[:HAS_TAG]->(tag:Tag)
            WHERE tag.id in tags
            RETURN
                friend.id AS personId,
                friend.firstName AS personFirstName,
                friend.lastName AS personLastName,
                collect(DISTINCT tag.name) AS tagNames,
                count(DISTINCT comment) AS replyCount
            ORDER BY
                replyCount DESC,
                toInteger(personId) ASC
            LIMIT 20""";
    public InteractiveComplex12() {
        this.id = 0;
    }

    @Override
    public Query getNewQuery() {
        return null;
    }

    // used for warmup
    public Query getDefaultQuery() {
        return new GraphQuery( cypher.replace("$personId",  "10995116278009").replace("$tagClassName", "'Monarch'") );
    }
}
