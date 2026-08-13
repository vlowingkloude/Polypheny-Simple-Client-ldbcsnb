/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-7/4/24, 10:20 AM The Polypheny Project
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

package org.polypheny.simpleclient.main;

import org.polypheny.simpleclient.executor.Executor;
import org.polypheny.simpleclient.executor.ExecutorException;
import org.polypheny.simpleclient.scenario.ldbcsnb.LdbcSnbBench;
import org.polypheny.simpleclient.scenario.ldbcsnb.LdbcSnbConfig;
import org.polypheny.simpleclient.scenario.ldbcsnb.LdbcSnbQueries;

import java.util.Locale;

public class LdbcSnbScenario {

    public static void schema( Executor.ExecutorFactory executorFactory, boolean commitAfterEveryQuery, LdbcSnbConfig config ) {
        LdbcSnbBench ldbcSnbBench = new LdbcSnbBench( executorFactory, commitAfterEveryQuery, false, config );
        ldbcSnbBench.createSchema(null, true);
    }


    public static void data( Executor.ExecutorFactory executorFactory, boolean commitAfterEveryQuery, LdbcSnbConfig config, String phase ) {
        LdbcSnbBench ldbcSnbBench = new LdbcSnbBench( executorFactory, commitAfterEveryQuery, false, config );
        ProgressReporter progressReporter = new ProgressBar( 1, 100 );
        switch ( phase.toLowerCase( Locale.ROOT ) ) {
            case "all" -> ldbcSnbBench.generateData( null, progressReporter, true, true );
            case "nodes" -> ldbcSnbBench.generateData( null, progressReporter, true, false );
            case "edges" -> ldbcSnbBench.generateData( null, progressReporter, false, true );
            default -> throw new IllegalArgumentException( "Unknown data phase: " + phase );
        }
    }


    public static void query( Executor.ExecutorFactory executorFactory, int queryNumber ) throws ExecutorException {
        Executor executor = executorFactory.createExecutorInstance( null, LdbcSnbBench.GRAPH_NAMESPACE );
        try {
            long elapsedNanos = executor.executeQuery( LdbcSnbQueries.complex( queryNumber ).getDefaultQuery() );
            System.out.printf( Locale.ROOT, "Complex query %d: PASS (%.3f ms)%n", queryNumber, elapsedNanos / 1_000_000.0 );
        } finally {
            close( executor );
        }
    }


    public static int smoke( Executor.ExecutorFactory executorFactory ) {
        int failures = 0;
        for ( int queryNumber : LdbcSnbQueries.complexQueryNumbers() ) {
            long start = System.nanoTime();
            try {
                query( executorFactory, queryNumber );
            } catch ( Throwable t ) {
                failures++;
                double elapsedMillis = (System.nanoTime() - start) / 1_000_000.0;
                System.out.printf( Locale.ROOT, "Complex query %d: FAIL after %.3f ms (%s)%n",
                        queryNumber, elapsedMillis, rootMessage( t ) );
            }
        }
        System.out.printf( Locale.ROOT, "LDBC complex query smoke test: %d passed, %d failed%n", 14 - failures, failures );
        return failures;
    }


    private static String rootMessage( Throwable throwable ) {
        Throwable root = throwable;
        while ( root.getCause() != null ) {
            root = root.getCause();
        }
        String message = root.getMessage() == null ? root.getClass().getSimpleName() : root.getMessage();
        int lineBreak = message.indexOf( '\n' );
        if ( lineBreak >= 0 ) {
            message = message.substring( 0, lineBreak );
        }
        int maxLength = 300;
        return message.length() <= maxLength ? message : message.substring( 0, maxLength ) + "...";
    }


    private static void close( Executor executor ) {
        if ( executor == null ) {
            return;
        }
        try {
            executor.closeConnection();
        } catch ( ExecutorException ignored ) {
            // The HTTP executor is stateless; closing is best effort.
        }
    }
}
