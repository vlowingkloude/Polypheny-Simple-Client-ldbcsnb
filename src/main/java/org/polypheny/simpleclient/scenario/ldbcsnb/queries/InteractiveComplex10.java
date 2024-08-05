/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-8/3/24, 5:20 PM The Polypheny Project
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

public class InteractiveComplex10 extends QueryBuilder {
    int id;
    final String cypher = """
            // Q10. Friend recommendation
            /*
            :params { personId: 4398046511333, month: 5 }
            */
            MATCH (person:Person {id: $personId})-[:KNOWS*2..2]-(friend),
                   (friend)-[:IS_LOCATED_IN]->(city:City)
            WHERE NOT friend=person AND
                  NOT (friend)-[:KNOWS]-(person)
            WITH person, city, friend, datetime({epochMillis: friend.birthday}) as birthday
            WHERE  (birthday.month=$month AND birthday.day>=21) OR
                    (birthday.month=($month%12)+1 AND birthday.day<22)
            WITH DISTINCT friend, city, person
            OPTIONAL MATCH (friend)<-[:HAS_CREATOR]-(post:Post)
            WITH friend, city, collect(post) AS posts, person
            WITH friend,
                 city,
                 size(posts) AS postCount,
                 size([p IN posts WHERE (p)-[:HAS_TAG]->()<-[:HAS_INTEREST]-(person)]) AS commonPostCount
            RETURN friend.id AS personId,
                   friend.firstName AS personFirstName,
                   friend.lastName AS personLastName,
                   commonPostCount - (postCount - commonPostCount) AS commonInterestScore,
                   friend.gender AS personGender,
                   city.name AS personCityName
            ORDER BY commonInterestScore DESC, personId ASC
            LIMIT 10""";
    public InteractiveComplex10() {
        this.id = 0;
    }

    @Override
    public Query getNewQuery() {
        return null;
    }
    // used for warmup
    public Query getDefaultQuery() {
        return new GraphQuery( cypher.replace("$personId",  "4398046511333").replace("$month", "5") );
    }
}
