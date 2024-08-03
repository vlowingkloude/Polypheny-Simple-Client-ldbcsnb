/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-8/3/24, 5:21 PM The Polypheny Project
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

public class InteractiveComplex11 extends QueryBuilder {
    int id;
    final String cypher = """
            // Q11. Job referral
            /*
            :params {
              personId: 10995116277918,
              countryName: "Hungary",
              workFromYear: 2011
            }
            */
            MATCH (person:Person {id: $personId })-[:KNOWS*1..2]-(friend:Person)
            WHERE not(person=friend)
            WITH DISTINCT friend
            MATCH (friend)-[workAt:WORK_AT]->(company:Company)-[:IS_LOCATED_IN]->(:Country {name: $countryName })
            WHERE workAt.workFrom < $workFromYear
            RETURN
                    friend.id AS personId,
                    friend.firstName AS personFirstName,
                    friend.lastName AS personLastName,
                    company.name AS organizationName,
                    workAt.workFrom AS organizationWorkFromYear
            ORDER BY
                    organizationWorkFromYear ASC,
                    toInteger(personId) ASC,
                    organizationName DESC
            LIMIT 10""";
    public InteractiveComplex11() {
        this.id = 0;
    }

    @Override
    public Query getNewQuery() {
        return null;
    }
}
