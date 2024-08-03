/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-8/3/24, 5:24 PM The Polypheny Project
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

public class InteractiveComplex14 extends QueryBuilder {
    int id;
    final String cypher = """
            // Q14. Trusted connection paths
            // Requires the Neo4j Graph Data Science library
            /*
            :params { person1Id: 14, person2Id: 27 }
            */
            // Check whether a path exists -- if there is no path, the query will return an empty result
            MATCH
                path = shortestPath((person1 {id: $person1Id})-[:KNOWS*]-(person2 {id: $person2Id}))
                        
            // ----------------------------------------------------------------------------------------------------
            // the actual values are not important,
            // we are only interested in whether there is a row or not
            WITH 42 AS dummy
            // ----------------------------------------------------------------------------------------------------
                        
            MATCH (person1:Person {id: $person1Id}), (person2:Person {id: $person2Id})
            CALL gds.graph.project.cypher(
              apoc.create.uuidBase64(),
              'MATCH (p:Person) RETURN id(p) AS id',
              'MATCH
                  (pA:Person)-[knows:KNOWS]-(pB:Person),
                  (pA)<-[:HAS_CREATOR]-(m1:Message)-[r:REPLY_OF]-(m2:Message)-[:HAS_CREATOR]->(pB)
                WITH
                  id(pA) AS source,
                  id(pB) AS target,
                  count(r) AS numInteractions
                RETURN
                  source,
                  target,
                  CASE WHEN round(40-sqrt(numInteractions)) > 1 THEN round(40-sqrt(numInteractions)) ELSE 1 END AS weight
              '
            )
            YIELD graphName
                        
            // ----------------------------------------------------------------------------------------------------
            WITH person1, person2, graphName
            // ----------------------------------------------------------------------------------------------------
                        
            CALL gds.shortestPath.dijkstra.stream(
                graphName, {sourceNode: person1, targetNode: person2, relationshipWeightProperty: 'weight'}
            )
            YIELD index, sourceNode, targetNode, totalCost, nodeIds, costs, path
                        
            WITH path, totalCost, graphName
                        
            CALL gds.graph.drop(graphName, false)
            YIELD graphName as graphNameremoved
                        
            RETURN [person IN nodes(path) | person.id] AS personIdsInPath, totalCost AS pathWeight
            LIMIT 1""";
    public InteractiveComplex14() {
        this.id = 0;
    }

    @Override
    public Query getNewQuery() {
        return null;
    }
}
