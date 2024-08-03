/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-8/3/24, 5:23 PM The Polypheny Project
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

public class InteractiveComplex13 extends QueryBuilder {
    int id;
    final String cypher = """
            // Q13. Single shortest path
            /*
            :params { person1Id: 8796093022390, person2Id: 8796093022357 }
            */
            MATCH
                (person1:Person {id: $person1Id}),
                (person2:Person {id: $person2Id}),
                path = shortestPath((person1)-[:KNOWS*]-(person2))
            RETURN
                CASE path IS NULL
                    WHEN true THEN -1
                    ELSE length(path)
                END AS shortestPathLength""";
    public InteractiveComplex13() {
        this.id = 0;
    }

    @Override
    public Query getNewQuery() {
        return null;
    }
}
