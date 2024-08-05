/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-8/3/24, 4:30 PM The Polypheny Project
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

public class InteractiveComplex1 extends QueryBuilder {
    // read the next query par
    int id;
    final String cypher = """
            // Q1. Transitive friends with certain name
            /*
            :params { personId: 4398046511333, firstName: "Jose" }
            */
            MATCH (p:Person {id: $personId}), (friend:Person {firstName: $firstName})
                   WHERE NOT p=friend
                   WITH p, friend
                   MATCH path = shortestPath((p)-[:KNOWS*1..3]-(friend))
                   WITH min(length(path)) AS distance, friend
            ORDER BY
                distance ASC,
                friend.lastName ASC,
                toInteger(friend.id) ASC
            LIMIT 20
               \s
            MATCH (friend)-[:IS_LOCATED_IN]->(friendCity:City)
            OPTIONAL MATCH (friend)-[studyAt:STUDY_AT]->(uni:University)-[:IS_LOCATED_IN]->(uniCity:City)
            WITH friend, collect(
                CASE
                    WHEN uni IS NULL THEN null
                    ELSE [uni.name, studyAt.classYear, uniCity.name]
                END ) AS unis, friendCity, distance
               \s
            OPTIONAL MATCH (friend)-[workAt:WORK_AT]->(company:Company)-[:IS_LOCATED_IN]->(companyCountry:Country)
            WITH friend, collect(
                CASE
                    WHEN company IS NULL then null
                    ELSE [company.name, workAt.workFrom, companyCountry.name]
                END ) AS companies, unis, friendCity, distance
               \s
            RETURN
                friend.id AS friendId,
                friend.lastName AS friendLastName,
                distance AS distanceFromPerson,
                friend.birthday AS friendBirthday,
                friend.creationDate AS friendCreationDate,
                friend.gender AS friendGender,
                friend.browserUsed AS friendBrowserUsed,
                friend.locationIP AS friendLocationIp,
                friend.email AS friendEmails,
                friend.speaks AS friendLanguages,
                friendCity.name AS friendCityName,
                unis AS friendUniversities,
                companies AS friendCompanies
            ORDER BY
                distanceFromPerson ASC,
                friendLastName ASC,
                toInteger(friendId) ASC
            LIMIT 20
   \s""";

    public InteractiveComplex1() {
        this.id = 0;
    }
    @Override
    public Query getNewQuery() {
        return null;
    }

    // used for warmup
    public Query getDefaultQuery() {
        return new GraphQuery( cypher.replace("$personId",  "4398046511333").replace("$firstName", "'Jose'") );
    }
}
