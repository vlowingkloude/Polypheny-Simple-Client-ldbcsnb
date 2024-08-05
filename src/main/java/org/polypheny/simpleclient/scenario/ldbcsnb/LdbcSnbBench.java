/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-6/24/24, 9:02 AM The Polypheny Project
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

package org.polypheny.simpleclient.scenario.ldbcsnb;

import org.apache.commons.compress.archivers.examples.Expander;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.polypheny.simpleclient.QueryMode;
import org.polypheny.simpleclient.executor.Executor;
import org.polypheny.simpleclient.executor.ExecutorException;
import org.polypheny.simpleclient.executor.PolyphenyDbCypherExecutor;
import org.polypheny.simpleclient.executor.PolyphenyDbExecutor;
import org.polypheny.simpleclient.main.CsvWriter;
import org.polypheny.simpleclient.main.ProgressReporter;
import org.polypheny.simpleclient.scenario.Scenario;
import org.polypheny.simpleclient.scenario.graph.GraphBenchConfig;
import org.polypheny.simpleclient.scenario.graph.GraphInsert;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

public class LdbcSnbBench extends Scenario {

    public static final String GRAPH_NAMESPACE = "ldbcsnb";
    public static boolean EXPECTED_RESULT = true;

    private final int scaleFactor;
    private final Path workDir;
    private final GraphBenchConfig config;

    public LdbcSnbBench(Executor.ExecutorFactory executorFactory,  boolean commitAfterEveryQuery, boolean dumpQueryList, int scaleFactor, GraphBenchConfig config ) {
        super( executorFactory, commitAfterEveryQuery, dumpQueryList, QueryMode.TABLE );
        this.scaleFactor = scaleFactor;
        this.config = config;
        String tempDirPrefix = "polypheny-ldbcsnb";
        try {
            workDir = Files.createTempDirectory(tempDirPrefix);
            workDir.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createSchema(Executor.DatabaseInstance databaseInstance, boolean includingKeys) {
        Executor executor = null;
        try {
            executor = executorFactory.createExecutorInstance( null, GRAPH_NAMESPACE);
            ((PolyphenyDbCypherExecutor)executor).dropStore("hsqldb");
            ((PolyphenyDbCypherExecutor)executor).deployNeo4j();
            executor.executeQuery( new CreateGraphDatabase().getNewQuery() );
        } catch (Exception e) {
            throw new RuntimeException( "LDBC SNB benchmark: failed to create schema", e );
        } finally {
            commitAndCloseExecutor( executor );
        }
    }

    @Override
    public void generateData(Executor.DatabaseInstance databaseInstance, ProgressReporter progressReporter) {
        Executor executor1 = executorFactory.createExecutorInstance( null, GRAPH_NAMESPACE);
        assert executor1 instanceof PolyphenyDbExecutor;
        String baseUrl = "https://repository.surfsara.nl/datasets/cwi/ldbc-snb-bi/files/bi-sf%d-composite-projected-fk.tar.zst";
        String url = String.format(baseUrl, scaleFactor);
        String downloadedName = workDir.toFile().getAbsolutePath() + "/ldbc-snb-bi.tar.zst";
        String tarName = workDir.toFile().getAbsolutePath() + "/ldbc-snb-bi.tar";
        String dataPath = workDir.toFile().getAbsolutePath() + "/ldbc-snb-bi";

        // download pre-generated data from ldbc repo
        // tried to integrate data generation process to the our benchmark system, but currently (part of) ldbc snb data generation code depends on java 8...
        try {
            FileUtils.copyURLToFile(
                    new URL(url),
                    new File( downloadedName )
            );
        } catch (IOException e) {
            throw new RuntimeException( "LDBC SNB benchmark: failed to download data", e );
        }

        // decompress downloaded data
        try (InputStream in = Files.newInputStream(Paths.get(downloadedName));
             BufferedInputStream inputBuffer = new BufferedInputStream(in);
             OutputStream out = Files.newOutputStream(Paths.get(tarName));
             CompressorInputStream decompressor = new CompressorStreamFactory().createCompressorInputStream(inputBuffer)) {
            IOUtils.copy(decompressor, out);
            new Expander().expand(Paths.get(tarName), Paths.get(dataPath));
        } catch (CompressorException | IOException | ArchiveException e) {
            throw new RuntimeException( "LDBC SNB benchmark: failed to process LDBC SNB files", e );
        }

        // read and insert data from gzipped csv files
        InputStream fileStream = null;
        InputStream gzipStream = null;
        Reader decoder = null;
        BufferedReader bufferedReader = null;
        String path;
        String baseQuery, query, line;
        List<String> row;
        File[] files;
        for (EntityHandler handler : EntityHandler.getEntities()) {
            path = handler.getPath(dataPath);
            files = new File( path ).listFiles( (dir, name) -> name.endsWith(".csv.gz") );
            for (File file : files) {
                try {
                    fileStream = new FileInputStream(path + file.getName());
                    gzipStream = new GzipCompressorInputStream( fileStream );
                    decoder = new InputStreamReader( gzipStream, "UTF-8" );
                    bufferedReader = new BufferedReader( decoder );
                    bufferedReader.readLine(); // skip headers TODO: should we add a flag to indicate if a file has a header?
                    while ( ( line = bufferedReader.readLine() ) != null ) {
                        row = Arrays.asList(line.split("\\|"));
                        executor1.executeQuery( new GraphInsert(handler.getQuery(row)) );
                    }
                } catch (IOException | ExecutorException e) {
                    throw new RuntimeException( "LDBC SNB benchmark: failed to process LDBC SNB files", e );
                }
            }
        }

    }

    @Override
    public long execute(ProgressReporter progressReporter, CsvWriter csvWriter, File outputDirectory, int numberOfThreads) {

        return 1;
    }

    @Override
    public void warmUp(ProgressReporter progressReporter) {

    }

    @Override
    public void analyze(Properties properties, File outputDirectory) {

    }

    @Override
    public int getNumberOfInsertThreads() {
        return 1;
    }
}
