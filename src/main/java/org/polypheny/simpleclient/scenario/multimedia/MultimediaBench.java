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

package org.polypheny.simpleclient.scenario.multimedia;


import com.google.common.base.Joiner;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.polypheny.simpleclient.executor.Executor;
import org.polypheny.simpleclient.executor.ExecutorException;
import org.polypheny.simpleclient.main.ChronosAgent;
import org.polypheny.simpleclient.main.CsvWriter;
import org.polypheny.simpleclient.main.ProgressReporter;
import org.polypheny.simpleclient.query.QueryBuilder;
import org.polypheny.simpleclient.query.QueryListEntry;
import org.polypheny.simpleclient.scenario.Scenario;
import org.polypheny.simpleclient.scenario.multimedia.queryBuilder.CreateTable;
import org.polypheny.simpleclient.scenario.multimedia.queryBuilder.DeleteRandomTimeline;
import org.polypheny.simpleclient.scenario.multimedia.queryBuilder.InsertRandomTimeline;
import org.polypheny.simpleclient.scenario.multimedia.queryBuilder.SelectRandomAlbum;
import org.polypheny.simpleclient.scenario.multimedia.queryBuilder.SelectRandomTimeline;
import org.polypheny.simpleclient.scenario.multimedia.queryBuilder.SelectRandomUser;


@Slf4j
public class MultimediaBench extends Scenario {

    private final MultimediaConfig config;

    private final List<Long> measuredTimes;
    private final Map<Integer, String> queryTypes;
    private final Map<Integer, List<Long>> measuredTimePerQueryType;


    public MultimediaBench( Executor.ExecutorFactory executorFactory, MultimediaConfig config, boolean commitAfterEveryQuery, boolean dumpQueryList ) {
        super( executorFactory, commitAfterEveryQuery, dumpQueryList );
        this.config = config;

        measuredTimes = Collections.synchronizedList( new LinkedList<>() );
        queryTypes = new HashMap<>();
        measuredTimePerQueryType = new ConcurrentHashMap<>();

        //make sure the tmp folder exists
        new File( System.getProperty( "user.home" ), ".polypheny/tmp/" ).mkdirs();

        loadHumbleLibrary();
    }


    private void loadHumbleLibrary() {
        final String libraryName;
        if ( SystemUtils.IS_OS_WINDOWS ) {
            libraryName = "libhumblevideo-0.dll";
        } else if ( SystemUtils.IS_OS_LINUX ) {
            libraryName = "libhumblevideo.so";
        } else if ( SystemUtils.IS_OS_MAC ) {
            libraryName = "libhumblevideo.dylib";
        } else {
            throw new RuntimeException( "Unexpected system" );
        }
        File out = new File( libraryName );
        if ( !out.exists() ) {
            try (
                    InputStream is = getClass().getResourceAsStream( "/" + libraryName );
                    OutputStream os = new FileOutputStream( out )
            ) {
                IOUtils.copy( is, os );
            } catch ( IOException e ) {
                log.error( "Caught exception while loading humble video lib", e );
            }
        }
        System.load( out.getAbsolutePath() );
    }


    @Override
    public void createSchema( boolean includingKeys ) {
        log.info( "Creating schema..." );
        Executor executor = null;

        try {
            executor = executorFactory.createExecutorInstance();
            executor.executeQuery( (new CreateTable( "CREATE TABLE IF NOT EXISTS \"users\" (\"id\" INTEGER NOT NULL, \"firstName\" VARCHAR(1000) NOT NULL, \"lastName\" VARCHAR(1000) NOT NULL, \"email\" VARCHAR(1000) NOT NULL, \"password\" VARCHAR(1000) NOT NULL, \"profile_pic\" IMAGE NOT NULL, PRIMARY KEY(\"id\"))" )).getNewQuery() );
            executor.executeQuery( (new CreateTable( "CREATE TABLE IF NOT EXISTS \"album\" (\"id\" INTEGER NOT NULL, \"user_id\" INTEGER NOT NULL, \"name\" VARCHAR(200) NOT NULL, PRIMARY KEY(\"id\"))" )).getNewQuery() );
            executor.executeQuery( (new CreateTable( "CREATE TABLE IF NOT EXISTS \"media\" (\"id\" INTEGER NOT NULL, \"timestamp\" TIMESTAMP NOT NULL, \"album_id\" INTEGER NOT NULL, \"img\" IMAGE, \"video\" VIDEO, \"sound\" SOUND, PRIMARY KEY(\"id\"))" )).getNewQuery() );
            executor.executeQuery( (new CreateTable( "CREATE TABLE IF NOT EXISTS \"timeline\" (\"id\" INTEGER NOT NULL, \"timestamp\" TIMESTAMP NOT NULL, \"user_id\" INTEGER NOT NULL, \"message\" VARCHAR(2000), \"img\" IMAGE, \"video\" VIDEO, \"sound\" SOUND, PRIMARY KEY(\"id\"))" )).getNewQuery() );
            executor.executeQuery( (new CreateTable( "CREATE TABLE IF NOT EXISTS \"followers\" (\"user_id\" INTEGER NOT NULL,\"friend_id\" INTEGER NOT NULL, PRIMARY KEY(\"user_id\", \"friend_id\"))" )).getNewQuery() );
            executor.executeQuery( (new CreateTable( "TRUNCATE TABLE \"users\"" )).getNewQuery() );
            executor.executeQuery( (new CreateTable( "TRUNCATE TABLE \"album\"" )).getNewQuery() );
            executor.executeQuery( (new CreateTable( "TRUNCATE TABLE \"media\"" )).getNewQuery() );
            executor.executeQuery( (new CreateTable( "TRUNCATE TABLE \"timeline\"" )).getNewQuery() );
            executor.executeQuery( (new CreateTable( "TRUNCATE TABLE \"followers\"" )).getNewQuery() );

            if ( includingKeys ) {
                executor.executeQuery( (new CreateTable( "ALTER TABLE public.\"album\" ADD CONSTRAINT \"fk1\" FOREIGN KEY(\"user_id\") REFERENCES \"users\"(\"id\") ON UPDATE CASCADE ON DELETE CASCADE" )).getNewQuery() );
                executor.executeQuery( (new CreateTable( "ALTER TABLE public.\"media\" ADD CONSTRAINT \"fk2\" FOREIGN KEY(\"album_id\") REFERENCES \"album\"(\"id\") ON UPDATE CASCADE ON DELETE CASCADE" )).getNewQuery() );
                executor.executeQuery( (new CreateTable( "ALTER TABLE public.\"timeline\" ADD CONSTRAINT \"fk3\" FOREIGN KEY(\"user_id\") REFERENCES \"users\"(\"id\") ON UPDATE CASCADE ON DELETE CASCADE" )).getNewQuery() );
                executor.executeQuery( (new CreateTable( "ALTER TABLE public.\"followers\" ADD CONSTRAINT \"fk4\" FOREIGN KEY(\"user_id\") REFERENCES \"users\"(\"id\") ON UPDATE CASCADE ON DELETE CASCADE" )).getNewQuery() );
                executor.executeQuery( (new CreateTable( "ALTER TABLE public.\"followers\" ADD CONSTRAINT \"fk5\" FOREIGN KEY(\"friend_id\") REFERENCES \"users\"(\"id\") ON UPDATE CASCADE ON DELETE CASCADE" )).getNewQuery() );
            }
        } catch ( ExecutorException e ) {
            throw new RuntimeException( "Exception while creating schema", e );
        } finally {
            commitAndCloseExecutor( executor );
        }
    }


    @Override
    public void generateData( ProgressReporter progressReporter ) {
        log.info( "Generating data..." );
        Executor executor1 = executorFactory.createExecutorInstance();
        DataGenerator dataGenerator = new DataGenerator( executor1, config, progressReporter );

        try {
            dataGenerator.generateUsers();
        } catch ( ExecutorException e ) {
            throw new RuntimeException( "Exception while generating data", e );
        } finally {
            commitAndCloseExecutor( executor1 );
        }
    }


    @Override
    public long execute( ProgressReporter progressReporter, CsvWriter csvWriter, File outputDirectory, int numberOfThreads ) {

        log.info( "Preparing query list for the benchmark..." );
        List<QueryListEntry> queryList = new Vector<>();
        addNumberOfTimes( queryList, new SelectRandomUser( config.numberOfUsers ), config.read );
        addNumberOfTimes( queryList, new SelectRandomAlbum( config.numberOfUsers ), config.read );
        addNumberOfTimes( queryList, new SelectRandomTimeline( config.numberOfUsers ), config.read );
        addNumberOfTimes( queryList, new DeleteRandomTimeline( config.numberOfUsers * config.postsPerUser ), config.write );
        addNumberOfTimes( queryList, new InsertRandomTimeline( config.numberOfUsers, config.imgSize, config.numberOfFrames, config.fileSizeKB ), config.write );

        Collections.shuffle( queryList );

        // This dumps the sql queries independent of the selected interface
        if ( outputDirectory != null && dumpQueryList ) {
            log.info( "Dump query list..." );
            try {
                FileWriter fw = new FileWriter( outputDirectory.getPath() + File.separator + "queryList" );
                queryList.forEach( query -> {
                    try {
                        fw.append( query.query.getSql() ).append( "\n" );
                    } catch ( IOException e ) {
                        log.error( "Error while dumping query list", e );
                    }
                } );
                fw.close();
            } catch ( IOException e ) {
                log.error( "Error while dumping query list", e );
            }
        }

        log.info( "Executing benchmark..." );
        (new Thread( new ProgressReporter.ReportQueryListProgress( queryList, progressReporter ) )).start();
        long startTime = System.nanoTime();

        ArrayList<EvaluationThread> threads = new ArrayList<>();
        for ( int i = 0; i < numberOfThreads; i++ ) {
            threads.add( new EvaluationThread( queryList, executorFactory.createExecutorInstance( csvWriter ) ) );
        }

        EvaluationThreadMonitor threadMonitor = new EvaluationThreadMonitor( threads );
        threads.forEach( t -> t.setThreadMonitor( threadMonitor ) );

        for ( EvaluationThread thread : threads ) {
            thread.start();
        }

        for ( Thread thread : threads ) {
            try {
                thread.join();
            } catch ( InterruptedException e ) {
                throw new RuntimeException( "Unexpected interrupt", e );
            }
        }

        if ( threadMonitor.aborted ) {
            throw new RuntimeException( "Exception while executing benchmark", threadMonitor.exception );
        }

        long runTime = System.nanoTime() - startTime;
        log.info( "run time: {} s", runTime / 1000000000 );

        for ( EvaluationThread thread : threads ) {
            thread.closeExecutor();
        }

        return runTime;
    }


    @Override
    public void warmUp( ProgressReporter progressReporter, int iterations ) {
        log.info( "Warm-up..." );

        Executor executor = null;

        for ( int i = 0; i < iterations; i++ ) {
            try {
                executor = executorFactory.createExecutorInstance();
                if ( config.numberOfUsers > 0 ) {
                    executor.executeQuery( new SelectRandomUser( config.numberOfUsers ).getNewQuery() );
                    executor.executeQuery( new SelectRandomAlbum( config.numberOfUsers ).getNewQuery() );
                    executor.executeQuery( new SelectRandomTimeline( config.numberOfUsers ).getNewQuery() );
                    executor.executeQuery( new DeleteRandomTimeline( config.numberOfUsers * config.postsPerUser ).getNewQuery() );
                    executor.executeQuery( new InsertRandomTimeline( config.numberOfUsers, config.imgSize, config.numberOfFrames, config.fileSizeKB ).getNewQuery() );
                }
            } catch ( ExecutorException e ) {
                throw new RuntimeException( "Error while executing warm-up queries", e );
            } finally {
                commitAndCloseExecutor( executor );
            }
            try {
                Thread.sleep( 10000 );
            } catch ( InterruptedException e ) {
                throw new RuntimeException( "Unexpected interrupt", e );
            }
        }
    }


    private class EvaluationThread extends Thread {

        private final Executor executor;
        private final List<QueryListEntry> theQueryList;
        private boolean abort = false;
        @Setter
        private EvaluationThreadMonitor threadMonitor;


        EvaluationThread( List<QueryListEntry> queryList, Executor executor ) {
            super( "EvaluationThread" );
            this.executor = executor;
            theQueryList = queryList;
        }


        @Override
        public void run() {
            long measuredTimeStart;
            long measuredTime;
            QueryListEntry queryListEntry;

            while ( !theQueryList.isEmpty() && !abort ) {
                measuredTimeStart = System.nanoTime();
                try {
                    queryListEntry = theQueryList.remove( 0 );
                } catch ( IndexOutOfBoundsException e ) { // This is neither nice nor efficient...
                    // This can happen due to concurrency if two threads enter the while-loop and there is only one thread left
                    // Simply leaf the loop
                    break;
                }
                try {
                    executor.executeQuery( queryListEntry.query );
                } catch ( ExecutorException e ) {
                    log.error( "Caught exception while executing queries", e );
                    threadMonitor.notifyAboutError( e );
                    try {
                        executor.executeRollback();
                    } catch ( ExecutorException ex ) {
                        log.error( "Error while rollback", e );
                    }
                    throw new RuntimeException( e );
                }
                measuredTime = System.nanoTime() - measuredTimeStart;
                measuredTimes.add( measuredTime );
                measuredTimePerQueryType.get( queryListEntry.templateId ).add( measuredTime );
                if ( commitAfterEveryQuery ) {
                    try {
                        executor.executeCommit();
                    } catch ( ExecutorException e ) {
                        log.error( "Caught exception while committing", e );
                        threadMonitor.notifyAboutError( e );
                        try {
                            executor.executeRollback();
                        } catch ( ExecutorException ex ) {
                            log.error( "Error while rollback", e );
                        }
                        throw new RuntimeException( e );
                    }
                }
            }

            try {
                executor.executeCommit();
            } catch ( ExecutorException e ) {
                log.error( "Caught exception while committing", e );
                threadMonitor.notifyAboutError( e );
                try {
                    executor.executeRollback();
                } catch ( ExecutorException ex ) {
                    log.error( "Error while rollback", e );
                }
                throw new RuntimeException( e );
            }

            executor.flushCsvWriter();
        }


        public void abort() {
            this.abort = true;
        }


        public void closeExecutor() {
            commitAndCloseExecutor( executor );
        }

    }


    private class EvaluationThreadMonitor {

        private final List<EvaluationThread> threads;
        @Getter
        private Exception exception;
        @Getter
        private boolean aborted;


        public EvaluationThreadMonitor( List<EvaluationThread> threads ) {
            this.threads = threads;
            this.aborted = false;
        }


        public void abortAll() {
            this.aborted = true;
            threads.forEach( EvaluationThread::abort );
        }


        public void notifyAboutError( Exception e ) {
            exception = e;
            abortAll();
        }

    }


    @Override
    public void analyze( Properties properties ) {
        properties.put( "measuredTime", calculateMean( measuredTimes ) );

        measuredTimePerQueryType.forEach( ( templateId, time ) -> {
            properties.put( "queryTypes_" + templateId + "_mean", calculateMean( time ) );
            if ( ChronosAgent.STORE_INDIVIDUAL_QUERY_TIMES ) {
                properties.put( "queryTypes_" + templateId + "_all", Joiner.on( ',' ).join( time ) );
            }
            properties.put( "queryTypes_" + templateId + "_example", queryTypes.get( templateId ) );
        } );
        properties.put( "queryTypes_maxId", queryTypes.size() );
    }


    @Override
    public int getNumberOfInsertThreads() {
        return 1;
    }


    private void addNumberOfTimes( List<QueryListEntry> list, QueryBuilder queryBuilder, int numberOfTimes ) {
        int id = queryTypes.size() + 1;
        queryTypes.put( id, queryBuilder.getNewQuery().getSql() );
        measuredTimePerQueryType.put( id, Collections.synchronizedList( new LinkedList<>() ) );
        for ( int i = 0; i < numberOfTimes; i++ ) {
            list.add( new QueryListEntry( queryBuilder.getNewQuery(), id ) );
        }
    }


    private void commitAndCloseExecutor( Executor executor ) {
        if ( executor != null ) {
            try {
                executor.executeCommit();
            } catch ( ExecutorException e ) {
                try {
                    executor.executeRollback();
                } catch ( ExecutorException ex ) {
                    log.error( "Error while rollback connection", e );
                }
            }
            try {
                executor.closeConnection();
            } catch ( ExecutorException e ) {
                log.error( "Error while closing connection", e );
            }
        }
    }

}
