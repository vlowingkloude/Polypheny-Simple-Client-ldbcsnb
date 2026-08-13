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
import com.google.gson.JsonObject;
import org.polypheny.simpleclient.QueryMode;
import org.polypheny.simpleclient.executor.Executor;
import org.polypheny.simpleclient.executor.ExecutorException;
import org.polypheny.simpleclient.executor.PolyphenyDbCypherExecutor;
import org.polypheny.simpleclient.main.CsvWriter;
import org.polypheny.simpleclient.main.ProgressReporter;
import org.polypheny.simpleclient.scenario.Scenario;
import org.polypheny.simpleclient.scenario.graph.GraphInsert;
import org.polypheny.simpleclient.scenario.graph.GraphQuery;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class LdbcSnbBench extends Scenario {

    public static final String GRAPH_NAMESPACE = "ldbcsnb";
    public static boolean EXPECTED_RESULT = true;

    private final Path workDir;
    private final LdbcSnbConfig config;


    public LdbcSnbBench( Executor.ExecutorFactory executorFactory, boolean commitAfterEveryQuery, boolean dumpQueryList, LdbcSnbConfig config ) {
        super( executorFactory, commitAfterEveryQuery, dumpQueryList, QueryMode.TABLE );
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
            String storeName = config.storeName();
            if ( config.deployNeo4j() ) {
                storeName = ((PolyphenyDbCypherExecutor) executor).deployNeo4j();
            } else if ( config.neo4jHost() != null && !config.neo4jHost().isBlank() ) {
                storeName = "neo4j_ldbc";
                JsonObject settings = new JsonObject();
                settings.addProperty( "mode", "remote" );
                settings.addProperty( "host", config.neo4jHost() );
                settings.addProperty( "port", Integer.toString( config.neo4jPort() ) );
                settings.addProperty( "user", "neo4j" );
                settings.addProperty( "password", config.neo4jPassword() );
                ((PolyphenyDbCypherExecutor) executor).deployAdapter( storeName, "NEO4J", "STORE", settings.toString() );
            }
            executor.executeQuery( new CreateGraphDatabase( storeName ).getNewQuery() );
        } catch (Exception e) {
            throw new RuntimeException( "LDBC SNB benchmark: failed to create schema", e );
        } finally {
            commitAndCloseExecutor( executor );
        }
    }

    private Path prepareData() {
        if ( config.dataPath() != null ) {
            return config.dataPath();
        }

        String url = String.format( config.dataUrlTemplate(), config.scaleFactor() );
        Path downloaded = workDir.resolve( "ldbc-snb-bi.tar.zst" );
        Path tar = workDir.resolve( "ldbc-snb-bi.tar" );
        Path extracted = workDir.resolve( "ldbc-snb-bi" );

        try {
            FileUtils.copyURLToFile( new URL( url ), downloaded.toFile() );
            try (InputStream in = Files.newInputStream( downloaded );
                    BufferedInputStream inputBuffer = new BufferedInputStream( in );
                    OutputStream out = Files.newOutputStream( tar );
                    CompressorInputStream decompressor = new CompressorStreamFactory().createCompressorInputStream( inputBuffer )) {
                IOUtils.copy( decompressor, out );
            }
            new Expander().expand( tar, extracted );
            return extracted;
        } catch ( CompressorException | IOException | ArchiveException e ) {
            throw new RuntimeException( "LDBC SNB benchmark: failed to prepare data", e );
        }
    }


    private long importNodes( BufferedReader reader, NodeEntity handler, Executor executor, boolean interactiveLayout ) throws IOException, ExecutorException {
        StringBuilder query = new StringBuilder( "CREATE " );
        long rows = 0;
        String line;
        while ( (line = reader.readLine()) != null ) {
            String fragment = handler.getBatchQuery( parseRow( line, handler, interactiveLayout ) );
            int separatorLength = query.length() == "CREATE ".length() ? 0 : 2;
            if ( query.length() > "CREATE ".length()
                    && query.length() + separatorLength + fragment.length() > config.maxBatchCharacters() ) {
                executor.executeQuery( new GraphInsert( query.toString() ) );
                query = new StringBuilder( "CREATE " );
                separatorLength = 0;
            }
            if ( separatorLength > 0 ) {
                query.append( ", " );
            }
            query.append( fragment );
            rows++;
        }
        if ( query.length() > "CREATE ".length() ) {
            executor.executeQuery( new GraphInsert( query.toString() ) );
        }
        return rows;
    }


    private long importEdges( BufferedReader reader, EdgeEntity handler, Executor executor, boolean interactiveLayout ) throws IOException, ExecutorException {
        LinkedHashSet<String> matches = new LinkedHashSet<>();
        List<String> creates = new ArrayList<>();
        long rows = 0;
        String line;
        while ( (line = reader.readLine()) != null ) {
            Map.Entry<String, String> fragment = handler.getBatchQuery( parseRow( line, handler, interactiveLayout ) );
            if ( interactiveLayout ) {
                fragment = adaptInteractiveEdgeFragment( handler, fragment );
            }
            LinkedHashSet<String> candidateMatches = new LinkedHashSet<>( matches );
            candidateMatches.add( fragment.getKey() );
            List<String> candidateCreates = new ArrayList<>( creates );
            candidateCreates.add( fragment.getValue() );
            String candidate = buildEdgeQuery( candidateMatches, candidateCreates );
            if ( !creates.isEmpty() && candidate.length() > config.maxEdgeBatchCharacters() ) {
                executeEdgeBatch( executor, matches, creates );
                matches.clear();
                creates.clear();
            }
            matches.add( fragment.getKey() );
            creates.add( fragment.getValue() );
            rows++;
        }
        if ( !creates.isEmpty() ) {
            executeEdgeBatch( executor, matches, creates );
        }
        return rows;
    }


    private void executeEdgeBatch( Executor executor, Collection<String> matches, Collection<String> creates ) throws ExecutorException {
        String query = buildEdgeQuery( matches, creates );
        try {
            executor.executeQuery( new GraphInsert( query ) );
        } catch ( ExecutorException exception ) {
            System.err.println( "Failed relationship batch Cypher: " + query );
            throw exception;
        }
    }


    private String buildEdgeQuery( Collection<String> matches, Collection<String> creates ) {
        LinkedHashSet<String> endpointMatches = new LinkedHashSet<>();
        for ( String match : matches ) {
            endpointMatches.addAll( Arrays.asList( match.split( ", (?=\\()", -1 ) ) );
        }
        return "MATCH " + String.join( ", ", endpointMatches ) + " CREATE " + String.join( ", ", creates );
    }


    private record EntityInput( File[] files, boolean interactiveLayout ) {
    }


    private EntityInput findEntityInput( Path dataPath, EntityHandler handler ) throws IOException {
        Path biPath = Path.of( handler.getPath( dataPath.toString(), config.scaleFactor() ) );
        File[] biFiles = biPath.toFile().listFiles( (dir, name) -> name.endsWith( ".csv.gz" ) );
        if ( biFiles != null && biFiles.length > 0 ) {
            Arrays.sort( biFiles, Comparator.comparing( File::getName ) );
            return new EntityInput( biFiles, false );
        }

        Path interactiveRoot = findInteractiveRoot( dataPath );
        String relativeStem = interactiveRelativeStem( handler );
        Path relative = Path.of( relativeStem );
        Path directory = interactiveRoot.resolve( relative.getParent() );
        String stem = relative.getFileName().toString();
        File[] interactiveFiles = directory.toFile().listFiles(
                (dir, name) -> name.matches( java.util.regex.Pattern.quote( stem ) + "_[0-9]+_[0-9]+\\.csv" ) );
        if ( interactiveFiles == null || interactiveFiles.length == 0 ) {
            throw new IOException( "Missing LDBC entity input for " + handler.getClass().getSimpleName()
                    + "; checked " + biPath + " and " + directory.resolve( stem + "_*.csv" ) );
        }
        Arrays.sort( interactiveFiles, Comparator.comparing( File::getName ) );
        return new EntityInput( interactiveFiles, true );
    }


    private Path findInteractiveRoot( Path dataPath ) throws IOException {
        if ( Files.isDirectory( dataPath.resolve( "static" ) ) && Files.isDirectory( dataPath.resolve( "dynamic" ) ) ) {
            return dataPath;
        }
        try (var paths = Files.walk( dataPath, 2 )) {
            return paths.filter( Files::isDirectory )
                    .filter( path -> Files.isDirectory( path.resolve( "static" ) ) )
                    .filter( path -> Files.isDirectory( path.resolve( "dynamic" ) ) )
                    .findFirst()
                    .orElseThrow( () -> new IOException( "Cannot find an LDBC BI or Interactive dataset below " + dataPath ) );
        }
    }


    private String interactiveRelativeStem( EntityHandler handler ) {
        return switch ( handler.getClass().getSimpleName() ) {
            case "Place" -> "static/place";
            case "Organisation" -> "static/organisation";
            case "TagClass" -> "static/tagclass";
            case "Tag" -> "static/tag";
            case "Forum" -> "dynamic/forum";
            case "Person" -> "dynamic/person";
            case "Comment" -> "dynamic/comment";
            case "Post" -> "dynamic/post";
            case "PlaceIsPartOfPlace" -> "static/place_isPartOf_place";
            case "TagClassIsSubclassOfTagClass" -> "static/tagclass_isSubclassOf_tagclass";
            case "OrganisationIsLocatedInPlace" -> "static/organisation_isLocatedIn_place";
            case "TagHasTypeTagClass" -> "static/tag_hasType_tagclass";
            case "CommentHasCreatorPerson" -> "dynamic/comment_hasCreator_person";
            case "CommentIsLocatedInCountry" -> "dynamic/comment_isLocatedIn_place";
            case "CommentReplyOfComment" -> "dynamic/comment_replyOf_comment";
            case "CommentReplyOfPost" -> "dynamic/comment_replyOf_post";
            case "ForumContainerOfPost" -> "dynamic/forum_containerOf_post";
            case "ForumHasMemberPerson" -> "dynamic/forum_hasMember_person";
            case "ForumHasModeratorPerson" -> "dynamic/forum_hasModerator_person";
            case "ForumHasTagTag" -> "dynamic/forum_hasTag_tag";
            case "PersonHasInterestTag" -> "dynamic/person_hasInterest_tag";
            case "PersonIsLocatedInCity" -> "dynamic/person_isLocatedIn_place";
            case "PersonKnowsPerson" -> "dynamic/person_knows_person";
            case "PersonLikesComment" -> "dynamic/person_likes_comment";
            case "PersonLikesPost" -> "dynamic/person_likes_post";
            case "PostHasCreatorPerson" -> "dynamic/post_hasCreator_person";
            case "CommentHasTagTag" -> "dynamic/comment_hasTag_tag";
            case "PostHasTagTag" -> "dynamic/post_hasTag_tag";
            case "PostIsLocatedInCountry" -> "dynamic/post_isLocatedIn_place";
            case "PersonStudyAtUniversity" -> "dynamic/person_studyAt_organisation";
            case "PersonWorkAtCompany" -> "dynamic/person_workAt_organisation";
            default -> throw new IllegalArgumentException( "No Interactive input mapping for " + handler.getClass().getName() );
        };
    }


    private Map.Entry<String, String> adaptInteractiveEdgeFragment( EntityHandler handler, Map.Entry<String, String> fragment ) {
        String createClause = fragment.getValue();
        createClause = switch ( handler.getClass().getSimpleName() ) {
            case "ForumHasMemberPerson" -> createClause.replace( "creationDate", "joinDate" );
            case "PersonStudyAtUniversity", "PersonWorkAtCompany" ->
                    createClause.replace( "creationDate: 0.0, ", "" );
            case "CommentHasCreatorPerson", "CommentIsLocatedInCountry",
                    "CommentReplyOfComment", "CommentReplyOfPost", "ForumContainerOfPost",
                    "ForumHasModeratorPerson", "ForumHasTagTag", "PersonHasInterestTag",
                    "PersonIsLocatedInCity", "PostHasCreatorPerson", "CommentHasTagTag",
                    "PostHasTagTag", "PostIsLocatedInCountry" ->
                    createClause.replace( " {creationDate: 0.0}", "" );
            default -> createClause;
        };
        return Map.entry( fragment.getKey(), createClause );
    }


    private List<String> parseRow( String line, EntityHandler handler, boolean interactiveLayout ) {
        List<String> row = Arrays.asList( line.split( "\\|", -1 ) );
        if ( !interactiveLayout ) {
            return row;
        }

        return switch ( handler.getClass().getSimpleName() ) {
            case "PlaceIsPartOfPlace", "TagClassIsSubclassOfTagClass",
                    "OrganisationIsLocatedInPlace", "TagHasTypeTagClass" -> row;
            case "Forum" -> List.of( row.get( 2 ), row.get( 0 ), row.get( 1 ) );
            case "Person" -> List.of(
                    row.get( 5 ), row.get( 0 ), row.get( 1 ), row.get( 2 ), row.get( 3 ),
                    row.get( 4 ), row.get( 6 ), row.get( 7 ), row.get( 8 ), row.get( 9 ) );
            case "Comment" -> List.of( row.get( 1 ), row.get( 0 ), row.get( 2 ), row.get( 3 ), row.get( 4 ), row.get( 5 ) );
            case "Post" -> List.of(
                    row.get( 2 ), row.get( 0 ), row.get( 1 ), row.get( 3 ),
                    row.get( 4 ), row.get( 5 ), row.get( 6 ), row.get( 7 ) );
            case "ForumHasMemberPerson", "PersonKnowsPerson", "PersonLikesComment", "PersonLikesPost" ->
                    List.of( row.get( 2 ), row.get( 0 ), row.get( 1 ) );
            case "PersonStudyAtUniversity", "PersonWorkAtCompany" ->
                    List.of( "0", row.get( 0 ), row.get( 1 ), row.get( 2 ) );
            default -> handler instanceof EdgeEntity && row.size() == 2
                    ? List.of( "0", row.get( 0 ), row.get( 1 ) )
                    : row;
        };
    }


    private BufferedReader openReader( File file ) throws IOException {
        InputStream input = new FileInputStream( file );
        if ( file.getName().endsWith( ".gz" ) ) {
            input = new GzipCompressorInputStream( input );
        }
        return new BufferedReader( new InputStreamReader( input, java.nio.charset.StandardCharsets.UTF_8 ) );
    }

    @Override
    public void generateData(Executor.DatabaseInstance databaseInstance, ProgressReporter progressReporter) {
        generateData( databaseInstance, progressReporter, true, true );
    }


    public void generateData( Executor.DatabaseInstance databaseInstance, ProgressReporter progressReporter, boolean includeNodes, boolean includeEdges ) {
        Executor executor = executorFactory.createExecutorInstance( null, GRAPH_NAMESPACE );
        Path dataPath = prepareData();
        long expectedNodes = 0;
        long expectedEdges = 0;
        long importStart = System.nanoTime();
        boolean startReached = config.startEntity() == null || config.startEntity().isBlank();
        long remainingRowsToSkip = config.skipRows();
        try {
            for ( EntityHandler handler : EntityHandler.getEntities() ) {
                boolean nodeHandler = handler instanceof NodeEntity;
                if ( (nodeHandler && !includeNodes) || (!nodeHandler && !includeEdges) ) {
                    continue;
                }
                if ( !startReached ) {
                    if ( !handler.getClass().getSimpleName().equals( config.startEntity() ) ) {
                        continue;
                    }
                    startReached = true;
                }
                EntityInput input = findEntityInput( dataPath, handler );
                long importedRows = 0;
                long entityStart = System.nanoTime();
                for ( File file : input.files() ) {
                    try (BufferedReader reader = openReader( file )) {
                        reader.readLine();
                        while ( remainingRowsToSkip > 0 && reader.readLine() != null ) {
                            remainingRowsToSkip--;
                        }
                        importedRows += handler instanceof NodeEntity node
                                ? importNodes( reader, node, executor, input.interactiveLayout() )
                                : importEdges( reader, (EdgeEntity) handler, executor, input.interactiveLayout() );
                    }
                }
                if ( handler instanceof NodeEntity ) {
                    expectedNodes += importedRows;
                } else {
                    expectedEdges += importedRows;
                }
                if ( remainingRowsToSkip > 0 ) {
                    throw new IllegalArgumentException( "skipRows exceeds rows in " + handler.getClass().getSimpleName() );
                }
                remainingRowsToSkip = 0;
                double entitySeconds = (System.nanoTime() - entityStart) / 1_000_000_000.0;
                System.out.printf( Locale.ROOT, "Imported %d rows for %s in %.3f s%n",
                        importedRows, handler.getClass().getSimpleName(), entitySeconds );
            }

            if ( !startReached ) {
                throw new IllegalArgumentException( "Unknown start entity: " + config.startEntity() );
            }

            double importSeconds = (System.nanoTime() - importStart) / 1_000_000_000.0;
            System.out.printf( Locale.ROOT, "LDBC import submitted: %d nodes and %d edges in %.3f s%n",
                    expectedNodes, expectedEdges, importSeconds );
        } catch ( Exception e ) {
            throw new RuntimeException( "LDBC SNB benchmark: failed to import data", e );
        } finally {
            commitAndCloseExecutor( executor );
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
