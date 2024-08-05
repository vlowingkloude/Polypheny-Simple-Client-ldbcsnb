/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-8/3/24, 5:16 PM The Polypheny Project
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

public class InteractiveComplex7 extends QueryBuilder {
    int id = 0;
    final String cypher = """
            // Q7. Recent likers
            /*
            :params { personId: 4398046511268 }
            */
            MATCH (person:Person {id: $personId})<-[:HAS_CREATOR]-(message:Message)<-[like:LIKES]-(liker:Person)
                WITH liker, message, like.creationDate AS likeTime, person
                ORDER BY likeTime DESC, toInteger(message.id) ASC
                WITH liker, head(collect({msg: message, likeTime: likeTime})) AS latestLike, person
            RETURN
                liker.id AS personId,
                liker.firstName AS personFirstName,
                liker.lastName AS personLastName,
                latestLike.likeTime AS likeCreationDate,
                latestLike.msg.id AS commentOrPostId,
                coalesce(latestLike.msg.content, latestLike.msg.imageFile) AS commentOrPostContent,
                toInteger(floor(toFloat(latestLike.likeTime - latestLike.msg.creationDate)/1000.0)/60.0) AS minutesLatency,
                not((liker)-[:KNOWS]-(person)) AS isNew
            ORDER BY
                likeCreationDate DESC,
                toInteger(personId) ASC
            LIMIT 20""";
    public InteractiveComplex7() {
        this.id = 0;
    }

    @Override
    public Query getNewQuery() {
        return null;
    }
    // used for warmup
    public Query getDefaultQuery() {
        return new GraphQuery( cypher.replace("$personId",  "4398046511268") );
    }

}
