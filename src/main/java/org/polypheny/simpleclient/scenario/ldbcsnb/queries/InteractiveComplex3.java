/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-8/3/24, 5:10 PM The Polypheny Project
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

public class InteractiveComplex3 extends QueryBuilder {
    int id;
    final String cypher = """
            // Q3. Friends and friends of friends that have been to given countries
            /*
            :params {
              personId: 6597069766734,
              countryXName: "Angola",
              countryYName: "Colombia",
              startDate: 1275393600000,
              endDate: 1277812800000
            }
            */
            MATCH (countryX:Country {name: $countryXName }),
                  (countryY:Country {name: $countryYName }),
                  (person:Person {id: $personId })
            WITH person, countryX, countryY
            LIMIT 1
            MATCH (city:City)-[:IS_PART_OF]->(country:Country)
            WHERE country IN [countryX, countryY]
            WITH person, countryX, countryY, collect(city) AS cities
            MATCH (person)-[:KNOWS*1..2]-(friend)-[:IS_LOCATED_IN]->(city)
            WHERE NOT person=friend AND NOT city IN cities
            WITH DISTINCT friend, countryX, countryY
            MATCH (friend)<-[:HAS_CREATOR]-(message),
                  (message)-[:IS_LOCATED_IN]->(country)
            WHERE $endDate > message.creationDate >= $startDate AND
                  country IN [countryX, countryY]
            WITH friend,
                 CASE WHEN country=countryX THEN 1 ELSE 0 END AS messageX,
                 CASE WHEN country=countryY THEN 1 ELSE 0 END AS messageY
            WITH friend, sum(messageX) AS xCount, sum(messageY) AS yCount
            WHERE xCount>0 AND yCount>0
            RETURN friend.id AS friendId,
                   friend.firstName AS friendFirstName,
                   friend.lastName AS friendLastName,
                   xCount,
                   yCount,
                   xCount + yCount AS xyCount
            ORDER BY xyCount DESC, friendId ASC
            LIMIT 20""";
    public InteractiveComplex3() {
        this.id = 0;
    }

    @Override
    public Query getNewQuery() {
        return null;
    }

    // used for warmup
    public Query getDefaultQuery() {
        return new GraphQuery( cypher.replace("$personId",  "6597069766734").replace("$countryXName", "'Angola'")
                .replace("$countryYName", "'Colombia'").replace("$startDate", "1275393600000")
                .replace("$endDate", "1277812800000"));
    }

}
