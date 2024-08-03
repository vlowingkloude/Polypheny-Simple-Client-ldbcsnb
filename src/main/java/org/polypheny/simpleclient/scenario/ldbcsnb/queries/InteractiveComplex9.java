/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-8/3/24, 5:19 PM The Polypheny Project
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

public class InteractiveComplex9 extends QueryBuilder {
    int id;
    final String cypher = """
            // Q9. Recent messages by friends or friends of friends
            /*
            :params { personId: 4398046511268, maxDate: 1289908800000 }
            */
            MATCH (root:Person {id: $personId })-[:KNOWS*1..2]-(friend:Person)
            WHERE NOT friend = root
            WITH collect(distinct friend) as friends
            UNWIND friends as friend
                MATCH (friend)<-[:HAS_CREATOR]-(message:Message)
                WHERE message.creationDate < $maxDate
            RETURN
                friend.id AS personId,
                friend.firstName AS personFirstName,
                friend.lastName AS personLastName,
                message.id AS commentOrPostId,
                coalesce(message.content,message.imageFile) AS commentOrPostContent,
                message.creationDate AS commentOrPostCreationDate
            ORDER BY
                commentOrPostCreationDate DESC,
                message.id ASC
            LIMIT 20""";
    public InteractiveComplex9() {
        this.id = 0;
    }

    @Override
    public Query getNewQuery() {
        return null;
    }
}
