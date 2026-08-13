/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-2022 The Polypheny Project
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

package org.polypheny.simpleclient.cli;

import com.github.rvesse.airline.HelpOption;
import com.github.rvesse.airline.annotations.AirlineModule;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.polypheny.simpleclient.executor.Executor.ExecutorFactory;
import org.polypheny.simpleclient.executor.PolyphenyDbCypherExecutor.PolyphenyDbCypherExecutorFactory;
import org.polypheny.simpleclient.executor.PolyphenyVersionSwitch;
import org.polypheny.simpleclient.main.LdbcSnbScenario;
import org.polypheny.simpleclient.scenario.ldbcsnb.LdbcSnbConfig;


@Slf4j
@Command(name = "ldbcsnb", description = "Mode for quick testing of Polypheny-DB using the LDBC SNB benchmark.")
public class LdbcSnbCommand implements CliRunnable {

    @AirlineModule
    private HelpOption<LdbcSnbCommand> help;

    @Arguments(description = "Task { schema | data [all|nodes|edges] | query <1..14> | smoke }.")
    private List<String> args;


    @Option(name = { "-pdb", "--polyphenydb" }, title = "IP or Hostname", arity = 1, description = "IP or Hostname of the Polypheny-DB server (default: 127.0.0.1).")
    public static String polyphenyDbHost = "127.0.0.1";


    @Option(name = { "--port" }, title = "HTTP port", arity = 1, description = "Polypheny-DB HTTP port (default: 13137).")
    public int polyphenyDbPort = 13137;


    @Option(name = { "--scale-factor" }, title = "Scale factor", arity = 1, description = "LDBC SNB scale factor (default: 1).")
    public String scaleFactor = "1";


    @Option(name = { "--data-path" }, title = "Dataset root", arity = 1, description = "Existing extracted LDBC dataset root; skips downloading.")
    public String dataPath;


    @Option(name = { "--data-url" }, title = "URL template", arity = 1, description = "Dataset URL template containing a single %s scale-factor placeholder.")
    public String dataUrlTemplate = LdbcSnbConfig.DEFAULT_DATA_URL_TEMPLATE;


    @Option(name = { "--batch-characters" }, title = "Characters", arity = 1, description = "Maximum generated Cypher statement size (default: 5000).")
    public int maxBatchCharacters = 5000;


    @Option(name = { "--edge-batch-characters" }, title = "Characters", arity = 1, description = "Maximum generated relationship Cypher statement size (default: 5000).")
    public int maxEdgeBatchCharacters = 5000;


    @Option(name = { "--start-entity" }, title = "Entity handler", arity = 1, description = "Resume data import at this entity handler class name.")
    public String startEntity;


    @Option(name = { "--skip-rows" }, title = "Rows", arity = 1, description = "Skip this many source rows in --start-entity before resuming.")
    public long skipRows = 0;


    @Option(name = { "--store" }, title = "Store name", arity = 1, description = "Existing Polypheny store used by the graph namespace (default: hsqldb).")
    public String storeName = "hsqldb";


    @Option(name = { "--deploy-neo4j" }, arity = 0, description = "Deploy and use a Neo4j store instead of --store (default: false).")
    public boolean deployNeo4j = false;


    @Option(name = { "--neo4j-host" }, title = "Neo4j host", arity = 1, description = "Attach a remote Neo4j instance as the graph store.")
    public String neo4jHost;


    @Option(name = { "--neo4j-port" }, title = "Neo4j Bolt port", arity = 1, description = "Remote Neo4j Bolt port (default: 7687).")
    public int neo4jPort = 7687;


    @Option(name = { "--neo4j-password" }, title = "Neo4j password", arity = 1, description = "Password for the remote Neo4j user.")
    public String neo4jPassword;


    @Option(name = { "--writeCSV" }, arity = 0, description = "Write a CSV file containing execution times for all executed queries (default: false).")
    public boolean writeCsv = false;


    @Option(name = { "--queryList" }, arity = 0, description = "Dump all queries into a file (default: false).")
    public boolean dumpQueryList = false;


    @Override
    public int run() throws SQLException {
        PolyphenyVersionSwitch.initializeDefaults();
        if ( args == null || args.isEmpty() ) {
            System.err.println( "Missing task" );
            System.exit( 1 );
        }

        LdbcSnbConfig config = new LdbcSnbConfig(
                scaleFactor,
                dataPath == null ? null : Path.of( dataPath ),
                dataUrlTemplate,
                maxBatchCharacters,
                maxEdgeBatchCharacters,
                startEntity,
                skipRows,
                storeName,
                deployNeo4j,
                neo4jHost,
                neo4jPort,
                neo4jPassword );
        ExecutorFactory executorFactory = new PolyphenyDbCypherExecutorFactory( polyphenyDbHost, polyphenyDbPort );

        try {
            switch ( args.getFirst().toLowerCase() ) {
                case "data" -> {
                    if ( args.size() > 2 ) {
                        throw new IllegalArgumentException( "data accepts at most one phase: all, nodes, or edges" );
                    }
                    String phase = args.size() == 2 ? args.get( 1 ) : "all";
                    LdbcSnbScenario.data( executorFactory, true, config, phase );
                }
                case "schema" -> LdbcSnbScenario.schema( executorFactory, true, config );
                case "query" -> {
                    if ( args.size() != 2 ) {
                        throw new IllegalArgumentException( "query requires a query number in [1, 14]" );
                    }
                    LdbcSnbScenario.query( executorFactory, Integer.parseInt( args.get( 1 ) ) );
                }
                case "smoke" -> {
                    int failures = LdbcSnbScenario.smoke( executorFactory );
                    if ( failures != 0 ) {
                        System.exit( 1 );
                    }
                }
                default -> throw new IllegalArgumentException( "Unknown task: " + args.getFirst() );
            }
        } catch ( Throwable t ) {
            log.error( "Exception while executing LDBC SNB benchmark!", t );
            System.exit( 1 );
        }

        try {
            Thread.sleep( 2000 );
        } catch ( InterruptedException e ) {
            throw new RuntimeException( "Unexpected interrupt", e );
        }

        return 0;
    }

}
