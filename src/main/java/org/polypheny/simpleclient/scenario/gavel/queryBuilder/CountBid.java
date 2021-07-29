/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Databases and Information Systems Research Group, University of Basel, Switzerland
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
import kong.unirest.HttpRequest;
import kong.unirest.Unirest;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.polypheny.simpleclient.QueryView;
import org.polypheny.simpleclient.query.Query;
import org.polypheny.simpleclient.query.QueryBuilder;


public class CountBid extends QueryBuilder {

    private static final boolean EXPECT_RESULT = true;
    private final QueryView queryView;


    public CountBid( QueryView queryView ) {
        this.queryView = queryView;
    }


    @Override
    public Query getNewQuery() {
        return new CountBidQuery( queryView );
    }


    private static class CountBidQuery extends Query {

        private final QueryView queryView;


        public CountBidQuery( QueryView queryView ) {
            super( EXPECT_RESULT );
            this.queryView = queryView;
        }


        @Override
        public String getSql() {
            if ( queryView.equals( QueryView.VIEW ) ) {
                return "SELECT * FROM countBid";
            } else if ( queryView.equals( QueryView.MATERIALIZED ) ) {
                return "SELECT * FROM countBid_materialized";
            } else {
                return "SELECT count(*) as NUMBER FROM bid";
            }

        }


        @Override
        public String getParameterizedSqlQuery() {
            return getSql();
        }


        @Override
        public Map<Integer, ImmutablePair<DataTypes, Object>> getParameterValues() {
            return new HashMap<>();
        }


        @Override
        public HttpRequest<?> getRest() {
            if ( queryView.equals( QueryView.VIEW ) ) {
                return Unirest.get( "{protocol}://{host}:{port}/restapi/v1/res/public.countBid" );
                // .queryString( "public.countBid", "*");
            } else if ( queryView.equals( QueryView.MATERIALIZED ) ) {
                return Unirest.get( "{protocol}://{host}:{port}/restapi/v1/res/public.countBid_materialized" );
            } else {
                return Unirest.get( "{protocol}://{host}:{port}/restapi/v1/res/public.bid" )
                        .queryString( "_project", "public.bid.id@num(COUNT)" );
            }

        }

    }

}
