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

import lombok.extern.slf4j.Slf4j;
import org.polypheny.simpleclient.executor.Executor;
import org.polypheny.simpleclient.scenario.graph.GraphBenchConfig;
import org.polypheny.simpleclient.scenario.ldbcsnb.LdbcSnbBench;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

@Slf4j
public class LdbcSnbScenario {
    public static void schema(Executor.ExecutorFactory executorFactory, boolean commitAfterEveryQuery ) {
        GraphBenchConfig config = new GraphBenchConfig( getProperties(), 1 );
        LdbcSnbBench ldbcSnbBench = new LdbcSnbBench(executorFactory, commitAfterEveryQuery, false, 1, config);
        ldbcSnbBench.createSchema(null, true);
    }


    public static void data(Executor.ExecutorFactory executorFactory, int multiplier, boolean commitAfterEveryQuery ) {
        GraphBenchConfig config = new GraphBenchConfig( getProperties(), multiplier );
        LdbcSnbBench ldbcSnbBench = new LdbcSnbBench(executorFactory, commitAfterEveryQuery, false, 1, config);
        ProgressReporter progressReporter = new ProgressBar( 1, 100 );
        ldbcSnbBench.generateData( null, progressReporter );
    }

    private static Properties getProperties() {
        Properties props = new Properties();
        try {
            props.load( Objects.requireNonNull( ClassLoader.getSystemResourceAsStream( "org/polypheny/simpleclient/scenario/graph/graph.properties" ) ) );
        } catch ( IOException e ) {
            log.error( "Exception while reading properties file", e );
        }
        return props;
    }
}
