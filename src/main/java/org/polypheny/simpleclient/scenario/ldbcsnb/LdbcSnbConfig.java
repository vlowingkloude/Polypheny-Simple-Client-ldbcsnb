/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-2026 The Polypheny Project
 */

package org.polypheny.simpleclient.scenario.ldbcsnb;

import java.nio.file.Path;


public record LdbcSnbConfig(
        String scaleFactor,
        Path dataPath,
        String dataUrlTemplate,
        int maxBatchCharacters,
        int maxEdgeBatchCharacters,
        String startEntity,
        long skipRows,
        String storeName,
        boolean deployNeo4j,
        String neo4jHost,
        int neo4jPort,
        String neo4jPassword ) {

    public static final String DEFAULT_DATA_URL_TEMPLATE =
            "https://repository.surfsara.nl/datasets/cwi/ldbc-snb-bi/files/bi-sf%s-composite-projected-fk.tar.zst";


    public LdbcSnbConfig {
        if ( scaleFactor == null || scaleFactor.isBlank() ) {
            throw new IllegalArgumentException( "scaleFactor must not be blank" );
        }
        if ( maxBatchCharacters < 1 ) {
            throw new IllegalArgumentException( "maxBatchCharacters must be positive" );
        }
        if ( maxEdgeBatchCharacters < 1 ) {
            throw new IllegalArgumentException( "maxEdgeBatchCharacters must be positive" );
        }
        if ( skipRows < 0 ) {
            throw new IllegalArgumentException( "skipRows must not be negative" );
        }
        if ( skipRows > 0 && (startEntity == null || startEntity.isBlank()) ) {
            throw new IllegalArgumentException( "startEntity is required when skipRows is positive" );
        }
        if ( dataPath == null && (dataUrlTemplate == null || dataUrlTemplate.isBlank()) ) {
            throw new IllegalArgumentException( "dataUrlTemplate is required when dataPath is not set" );
        }
        if ( !deployNeo4j && (neo4jHost == null || neo4jHost.isBlank()) && (storeName == null || storeName.isBlank()) ) {
            throw new IllegalArgumentException( "storeName is required when Neo4j deployment is disabled" );
        }
        if ( neo4jHost != null && !neo4jHost.isBlank() ) {
            if ( neo4jPort < 1 || neo4jPort > 65535 ) {
                throw new IllegalArgumentException( "neo4jPort must be in [1, 65535]" );
            }
            if ( neo4jPassword == null || neo4jPassword.isBlank() ) {
                throw new IllegalArgumentException( "neo4jPassword is required for a remote Neo4j store" );
            }
        }
    }

}
