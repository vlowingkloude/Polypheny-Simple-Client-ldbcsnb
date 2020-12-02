package org.polypheny.simpleclient.executor;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import kong.unirest.HttpRequest;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.polypheny.simpleclient.executor.PolyphenyDbJdbcExecutor.PolyphenyDbJdbcExecutorFactory;
import org.polypheny.simpleclient.main.CsvWriter;
import org.polypheny.simpleclient.query.BatchableInsert;
import org.polypheny.simpleclient.query.Query;
import org.polypheny.simpleclient.query.RawQuery;
import org.polypheny.simpleclient.scenario.AbstractConfig;


@Slf4j
public class PolyphenyDbRestExecutor implements PolyphenyDbExecutor {

    private final PolyphenyDbJdbcExecutorFactory jdbcExecutorFactory;
    private final String host;

    private final CsvWriter csvWriter;


    public PolyphenyDbRestExecutor( String host, CsvWriter csvWriter ) {
        super();
        this.host = host;
        this.csvWriter = csvWriter;
        jdbcExecutorFactory = new PolyphenyDbJdbcExecutor.PolyphenyDbJdbcExecutorFactory( host, false );
    }


    @Override
    public void reset() throws ExecutorException {
        throw new RuntimeException( "Unsupported operation" );
    }


    @Override
    public long executeQuery( Query query ) throws ExecutorException {
        long time;
        if ( query.getRest() != null ) {
            HttpRequest<?> request = query.getRest();
            request.basicAuth( "pa", "" );
            request.routeParam( "protocol", "http" );
            request.routeParam( "host", host );
            request.routeParam( "port", "8089" );
            log.debug( request.getUrl() );
            try {
                long start = System.nanoTime();
                HttpResponse<JsonNode> result = request.asJson();
                if ( !result.isSuccess() ) {
                    throw new ExecutorException( "Error while executing REST query. Message: " + result.getStatusText() + "  |  URL: " + request.getUrl() );
                }
                time = System.nanoTime() - start;
                if ( csvWriter != null ) {
                    csvWriter.appendToCsv( request.getUrl(), time );
                }
            } catch ( UnirestException e ) {
                throw new ExecutorException( e );
            }
        } else {
            // There is no REST expression available for this query. Executing SQL expression via JDBC.
            log.warn( query.getSql() );
            JdbcExecutor executor = null;
            try {
                executor = jdbcExecutorFactory.createExecutorInstance( csvWriter );
                time = executor.executeQuery( query );
                if ( csvWriter != null ) {
                    csvWriter.appendToCsv( query.getSql(), time );
                }
            } catch ( ExecutorException e ) {
                throw new ExecutorException( "Error while executing query via JDBC", e );
            } finally {
                commitAndCloseJdbcExecutor( executor );
            }
        }

        return time;
    }


    @Override
    public long executeQueryAndGetNumber( Query query ) throws ExecutorException {
        if ( query.getRest() != null ) {
            HttpRequest<?> request = query.getRest();
            request.basicAuth( "pa", "" );
            request.routeParam( "protocol", "http" );
            request.routeParam( "host", host );
            request.routeParam( "port", "8089" );
            log.debug( request.getUrl() );
            try {
                long start = System.nanoTime();
                HttpResponse<JsonNode> result = request.asJson();
                if ( !result.isSuccess() ) {
                    throw new ExecutorException( "Error while executing REST query. Message: " + result.getStatusText() + "  |  URL: " + request.getUrl() );
                }
                if ( csvWriter != null ) {
                    csvWriter.appendToCsv( request.getUrl(), System.nanoTime() - start );
                }
                // Get result of a count query
                JSONArray res = result.getBody().getObject().getJSONArray( "result" );
                if ( res.length() != 1 ) {
                    throw new ExecutorException( "Invalid result: " + res.toString() );
                }
                Set<String> names = res.getJSONObject( 0 ).keySet();
                if ( names.size() != 1 ) {
                    throw new ExecutorException( "Invalid result: " + res.toString() );
                }
                return res.getJSONObject( 0 ).getLong( names.iterator().next() );
            } catch ( UnirestException e ) {
                throw new ExecutorException( e );
            }
        } else {
            // There is no REST expression available for this query. Executing SQL expression via JDBC.
            log.warn( query.getSql() );
            JdbcExecutor executor = null;
            try {
                executor = jdbcExecutorFactory.createExecutorInstance( csvWriter );
                return executor.executeQueryAndGetNumber( query );
            } catch ( ExecutorException e ) {
                throw new ExecutorException( "Error while executing query via JDBC", e );
            } finally {
                commitAndCloseJdbcExecutor( executor );
            }
        }
    }


    @Override
    public void executeCommit() throws ExecutorException {
        // NoOp
    }


    @Override
    public void executeRollback() throws ExecutorException {
        log.error( "Unsupported operation: Rollback" );
    }


    @Override
    public void closeConnection() throws ExecutorException {
        // NoOp
    }


    @Override
    public void executeInsertList( List<BatchableInsert> batchList, AbstractConfig config ) throws ExecutorException {
        String currentTable = null;
        List<JsonObject> rows = new ArrayList<>();
        for ( BatchableInsert query : batchList ) {
            if ( currentTable == null ) {
                currentTable = query.getTable();
            }

            if ( currentTable.equals( query.getTable() ) ) {
                rows.add( Objects.requireNonNull( query.getRestRowExpression() ) );
            } else {
                throw new RuntimeException( "Different tables in multi-inserts. This should not happen!" );
            }
        }
        if ( rows.size() > 0 ) {
            executeQuery( new RawQuery( null, Query.buildRestInsert( currentTable, rows ), false ) );
        }
    }


    @Override
    public void dropStore( String name ) throws ExecutorException {
        PolyphenyDbJdbcExecutor executor = null;
        try {
            executor = jdbcExecutorFactory.createExecutorInstance( csvWriter );
            executor.dropStore( name );
            executor.executeCommit();
        } catch ( ExecutorException e ) {
            throw new ExecutorException( "Error while executing query via JDBC", e );
        } finally {
            commitAndCloseJdbcExecutor( executor );
        }
    }


    @Override
    public void deployStore( String name, String clazz, String config ) throws ExecutorException {
        PolyphenyDbJdbcExecutor executor = null;
        try {
            executor = jdbcExecutorFactory.createExecutorInstance( csvWriter );
            executor.deployStore( name, clazz, config );
            executor.executeCommit();
        } catch ( ExecutorException e ) {
            throw new ExecutorException( "Error while executing query via JDBC", e );
        } finally {
            commitAndCloseJdbcExecutor( executor );
        }
    }


    @Override
    public void setConfig( String key, String value ) {
        PolyphenyDbJdbcExecutor executor = null;
        try {
            executor = jdbcExecutorFactory.createExecutorInstance( csvWriter );
            executor.setConfig( key, value );
            executor.executeCommit();
        } catch ( ExecutorException e ) {
            log.error( "Exception while setting config \"" + key + "\"!", e );
        } finally {
            try {
                commitAndCloseJdbcExecutor( executor );
            } catch ( ExecutorException e ) {
                log.error( "Exception while closing JDBC executor", e );
            }
        }
    }


    private void commitAndCloseJdbcExecutor( JdbcExecutor executor ) throws ExecutorException {
        if ( executor != null ) {
            try {
                executor.executeCommit();
            } catch ( ExecutorException e ) {
                try {
                    executor.executeRollback();
                } catch ( ExecutorException ex ) {
                    log.error( "Error while rollback connection", e );
                }
            } finally {
                try {
                    executor.closeConnection();
                } catch ( ExecutorException e ) {
                    log.error( "Error while closing connection", e );
                }
            }
        }
    }


    @Override
    public void flushCsvWriter() {
        if ( csvWriter != null ) {
            try {
                csvWriter.flush();
            } catch ( IOException e ) {
                log.warn( "Exception while flushing csv writer", e );
            }
        }
    }


    public static class PolyphenyDbRestExecutorFactory extends ExecutorFactory {

        private final String host;


        public PolyphenyDbRestExecutorFactory( String host ) {
            this.host = host;
        }


        @Override
        public PolyphenyDbRestExecutor createExecutorInstance( CsvWriter csvWriter ) {
            return new PolyphenyDbRestExecutor( host, csvWriter );
        }


        @Override
        public int getMaxNumberOfThreads() {
            return 0;
        }

    }

}
