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
import java.util.concurrent.ThreadLocalRandom;
import kong.unirest.HttpRequest;
import kong.unirest.Unirest;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.polypheny.simpleclient.QueryView;
import org.polypheny.simpleclient.query.Query;
import org.polypheny.simpleclient.query.QueryBuilder;


public class SelectAllBidsOnRandomAuction extends QueryBuilder {

    private static final boolean EXPECT_RESULT = true;

    private final int numberOfAuctions;
    private final QueryView queryView;


    public SelectAllBidsOnRandomAuction( int numberOfAuctions, QueryView queryView ) {
        this.numberOfAuctions = numberOfAuctions;
        this.queryView = queryView;
    }


    @Override
    public Query getNewQuery() {
        int auctionId = ThreadLocalRandom.current().nextInt( 1, numberOfAuctions + 1 );
        return new SelectAllBidsOnRandomAuctionQuery( auctionId, queryView );
    }


    private static class SelectAllBidsOnRandomAuctionQuery extends Query {

        private final int auctionId;
        private final String tablename;


        public SelectAllBidsOnRandomAuctionQuery( int auctionId, QueryView queryView ) {
            super( EXPECT_RESULT );
            this.auctionId = auctionId;

            if ( queryView.equals( QueryView.VIEW ) ) {
                tablename = "bid_view";
            } else if ( queryView.equals( QueryView.MATERIALIZED ) ) {
                tablename = "bid_materialized";
            } else {
                tablename = "bid";
            }
        }


        @Override
        public String getSql() {
            return "SELECT * FROM " + tablename + " b WHERE b.auction=" + auctionId;
        }


        @Override
        public String getParameterizedSqlQuery() {
            return "SELECT * FROM " + tablename + " b WHERE b.auction=?";
        }


        @Override
        public Map<Integer, ImmutablePair<DataTypes, Object>> getParameterValues() {
            Map<Integer, ImmutablePair<DataTypes, Object>> map = new HashMap<>();
            map.put( 1, new ImmutablePair<>( DataTypes.INTEGER, auctionId ) );
            return map;
        }


        @Override
        public HttpRequest<?> getRest() {
            return Unirest.get( "{protocol}://{host}:{port}/restapi/v1/res/public." + tablename )
                    .queryString( "public." + tablename + ".auction", "=" + auctionId );
        }

    }

}
