/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-2021 The Polypheny Project
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
 *
 */

package org.polypheny.simpleclient.scenario.gavel.queryBuilder;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import kong.unirest.HttpRequest;
import kong.unirest.Unirest;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.polypheny.simpleclient.QueryMode;
import org.polypheny.simpleclient.query.Query;
import org.polypheny.simpleclient.query.QueryBuilder;


public class SelectRandomUser extends QueryBuilder {

    private static final boolean EXPECT_RESULT = true;

    private final int numberOfUsers;
    private final QueryMode queryMode;


    public SelectRandomUser( int numberOfUsers, QueryMode queryMode ) {
        this.numberOfUsers = numberOfUsers;
        this.queryMode = queryMode;
    }


    @Override
    public Query getNewQuery() {
        int userId = ThreadLocalRandom.current().nextInt( 1, numberOfUsers + 1 );
        return new SelectRandomUserQuery( userId, queryMode );
    }


    private static class SelectRandomUserQuery extends Query {

        private final int userId;
        private final String tableName;


        public SelectRandomUserQuery( int userId, QueryMode queryMode ) {
            super( EXPECT_RESULT );
            this.userId = userId;

            if ( queryMode.equals( QueryMode.VIEW ) ) {
                tableName = "user_view";
            } else if ( queryMode.equals( QueryMode.MATERIALIZED ) ) {
                tableName = "user_materialized";
            } else {
                tableName = "\"user\"";
            }
        }


        @Override
        public String getSql() {
            return "SELECT * FROM " + tableName + " WHERE id=" + userId;
        }


        @Override
        public String getParameterizedSqlQuery() {
            return "SELECT * FROM " + tableName + " WHERE id=?";
        }


        @Override
        public Map<Integer, ImmutablePair<DataTypes, Object>> getParameterValues() {
            Map<Integer, ImmutablePair<DataTypes, Object>> map = new HashMap<>();
            map.put( 1, new ImmutablePair<>( DataTypes.INTEGER, userId ) );
            return map;
        }


        @Override
        public HttpRequest<?> getRest() {
            return Unirest.get( "{protocol}://{host}:{port}/restapi/v1/res/public." + tableName )
                    .queryString( "public." + tableName + ".id", "=" + userId );
        }


        @Override
        public String getMongoQl() {
            return "db." + tableName.replace( "\"", "" ) + ".find({\"id\":" + userId + "})";
        }

    }

}
