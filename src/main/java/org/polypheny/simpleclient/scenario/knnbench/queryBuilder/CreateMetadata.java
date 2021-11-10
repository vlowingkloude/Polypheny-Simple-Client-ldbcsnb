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

package org.polypheny.simpleclient.scenario.knnbench.queryBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import kong.unirest.HttpRequest;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.polypheny.simpleclient.query.CottontailQuery;
import org.polypheny.simpleclient.query.CottontailQuery.QueryType;
import org.polypheny.simpleclient.query.Query;
import org.polypheny.simpleclient.query.QueryBuilder;
import org.vitrivr.cottontail.grpc.CottontailGrpc.ColumnDefinition;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Entity;
import org.vitrivr.cottontail.grpc.CottontailGrpc.EntityDefinition;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Schema;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Type;


public class CreateMetadata extends QueryBuilder {

    private final String store;


    public CreateMetadata( String store ) {
        this.store = store;
    }


    @Override
    public Query getNewQuery() {
        return new CreateMetadataQuery( this.store );
    }


    private static class CreateMetadataQuery extends Query {

        private final String store;


        CreateMetadataQuery( String store ) {
            super( false );
            this.store = store;
        }


        @Override
        public String getSql() {
            String sql = "CREATE TABLE knn_metadata (id integer NOT NULL, textdata VARCHAR(100), PRIMARY KEY (id))";
            if ( this.store != null ) {
                sql += "ON STORE \"" + this.store + "\"";
            }
            return sql;
        }


        @Override
        public String getParameterizedSqlQuery() {
            return null;
        }


        @Override
        public Map<Integer, ImmutablePair<DataTypes, Object>> getParameterValues() {
            return null;
        }


        @Override
        public HttpRequest<?> getRest() {
            return null;
        }


        @Override
        public String getMongoQl() {
            return null;
        }


        @Override
        public CottontailQuery getCottontail() {
            List<ColumnDefinition> columns = new ArrayList<>();
            columns.add( ColumnDefinition.newBuilder().setName( "id" ).setType( Type.INTEGER ).build() );
            columns.add( ColumnDefinition.newBuilder().setName( "textdata" ).setType( Type.STRING ).build() );
            EntityDefinition entityDefinition = EntityDefinition.newBuilder()
                    .setEntity( Entity.newBuilder().setSchema( Schema.newBuilder().setName( "public" ).build() ).setName( "knn_metadata" ).build() )
                    .addAllColumns( columns )
                    .build();
            return new CottontailQuery(
                    QueryType.ENTITY_CREATE,
                    entityDefinition
            );
        }

    }

}
