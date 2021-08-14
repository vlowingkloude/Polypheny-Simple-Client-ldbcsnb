package org.polypheny.simpleclient.scenario.knnbench;


import java.util.Map;
import java.util.Properties;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.polypheny.simpleclient.scenario.AbstractConfig;


@Slf4j
public class KnnBenchConfig extends AbstractConfig {

    public final String dataStoreFeature;
    public final String dataStoreMetadata;

    public final long randomSeedInsert;
    public final long randomSeedQuery;

    public final int dimensionFeatureVectors;
    public final int batchSizeInserts;
    public final int batchSizeQueries;

    public final int numberOfEntries;
    public final int numberOfSimpleKnnIntFeatureQueries;
    public final int numberOfSimpleKnnRealFeatureQueries;
    public final int numberOfSimpleMetadataQueries;
    public final int numberOfSimpleKnnIdIntFeatureQueries;
    public final int numberOfSimpleKnnIdRealFeatureQueries;
    public final int numberOfMetadataKnnIntFeatureQueries;
    public final int numberOfMetadataKnnRealFeatureQueries;
//    public final int numberOfCombinedQueries;

    public final int limitKnnQueries;
    public final String distanceNorm;


    public KnnBenchConfig( Properties properties, int multiplier ) {
        super( "knnBench", "polypheny" );

        pdbBranch = null;
        puiBranch = null;
        buildUi = false;
        resetCatalog = false;
        memoryCatalog = false;
        deployStoresUsingDocker = false;

        router = "icarus"; // For old routing, to be removed

        routers = new String[]{ "Simple", "Icarus", "FullPlacement" };
        newTablePlacementStrategy = "Single";
        planSelectionStrategy = "Best";
        preCostRatio = 50;
        postCostRatio = 50;
        routingCache = true;
        postCostAggregation = true;

        workloadMonitoringProcessingInterval = "5s";
        workloadMonitoringElementsPerInterval = 50;

        planAndImplementationCaching = "Both";

        dataStoreFeature = null;
        dataStoreMetadata = null;
        //dataStores.add( "cottontail" );

        progressReportBase = getIntProperty( properties, "progressReportBase" );
        numberOfThreads = getIntProperty( properties, "numberOfThreads" );
        numberOfWarmUpIterations = getIntProperty( properties, "numberOfWarmUpIterations" );

        if ( getBooleanProperty( properties, "useRandomSeeds" ) ) {
            Random tempRand = new Random();
            randomSeedInsert = tempRand.nextLong();
            randomSeedQuery = tempRand.nextLong();
        } else {
            randomSeedInsert = getLongProperty( properties, "randomSeedInsert" );
            randomSeedQuery = getLongProperty( properties, "randomSeedQuery" );
        }

        dimensionFeatureVectors = getIntProperty( properties, "dimensionFeatureVectors" );
        batchSizeInserts = getIntProperty( properties, "batchSizeInserts" );
        numberOfEntries = getIntProperty( properties, "numberOfEntries" ) * multiplier;

        batchSizeQueries = getIntProperty( properties, "batchSizeQueries" );
        numberOfSimpleKnnIntFeatureQueries = getIntProperty( properties, "numberOfSimpleKnnIntFeatureQueries" ) * multiplier;
        numberOfSimpleKnnRealFeatureQueries = getIntProperty( properties, "numberOfSimpleKnnRealFeatureQueries" ) * multiplier;
        numberOfSimpleMetadataQueries = getIntProperty( properties, "numberOfSimpleMetadataQueries" ) * multiplier;
        numberOfSimpleKnnIdIntFeatureQueries = getIntProperty( properties, "numberOfSimpleKnnIdIntFeatureQueries" ) * multiplier;
        numberOfSimpleKnnIdRealFeatureQueries = getIntProperty( properties, "numberOfSimpleKnnIdRealFeatureQueries" ) * multiplier;
        numberOfMetadataKnnIntFeatureQueries = getIntProperty( properties, "numberOfMetadataKnnIntFeatureQueries" ) * multiplier;
        numberOfMetadataKnnRealFeatureQueries = getIntProperty( properties, "numberOfMetadataKnnRealFeatureQueries" ) * multiplier;
        limitKnnQueries = getIntProperty( properties, "limitKnnQueries" );
        distanceNorm = getStringProperty( properties, "distanceNorm" );
    }


    public KnnBenchConfig( Map<String, String> cdl ) {
        super( "gavel", cdl.get( "store" ) );

        pdbBranch = cdl.get( "pdbBranch" );
        puiBranch = cdl.get( "puiBranch" );
        buildUi = Boolean.parseBoolean( cdlGetOrDefault( cdl, "buildUi", "false" ) );
        resetCatalog = Boolean.parseBoolean( cdl.get( "resetCatalog" ) );
        memoryCatalog = Boolean.parseBoolean( cdl.get( "memoryCatalog" ) );

        deployStoresUsingDocker = Boolean.parseBoolean( cdlGetOrDefault( cdl, "deployStoresUsingDocker", "false" ) );

        dataStoreFeature = cdl.get( "dataStoreFeature" );
        dataStoreMetadata = cdl.get( "dataStoreMetadata" );
        if ( dataStoreFeature.equals( dataStoreMetadata ) ) {
            dataStores.add( dataStoreFeature );
        } else {
            dataStores.add( dataStoreFeature );
            dataStores.add( dataStoreMetadata );
        }

        router = cdl.get( "router" ); // For old routing, to be removed

        routers = cdlGetOrDefault( cdl, "routers", "Simple_Icarus_FullPlacement" ).split( "_" );
        newTablePlacementStrategy = cdlGetOrDefault( cdl, "newTablePlacementStrategy", "Single" );
        planSelectionStrategy = cdlGetOrDefault( cdl, "planSelectionStrategy", "Best" );

        preCostRatio = Integer.parseInt( cdlGetOrDefault( cdl, "preCostRatio", "50%" ).replace( "%", "" ).trim() );
        postCostRatio = Integer.parseInt( cdlGetOrDefault( cdl, "postCostRatio", "50%" ).replace( "%", "" ).trim() );
        routingCache = Boolean.parseBoolean( cdlGetOrDefault( cdl, "routingCache", "true" ) );
        postCostAggregation = Boolean.parseBoolean( cdlGetOrDefault( cdl, "postCostAggregation", "true" ) );

        workloadMonitoringProcessingInterval = cdlGetOrDefault( cdl, "workloadMonitoringProcessingInterval", "5s" );
        workloadMonitoringElementsPerInterval = Integer.parseInt( cdlGetOrDefault( cdl, "workloadMonitoringElementsPerInterval", "50" ) );

        planAndImplementationCaching = cdlGetOrDefault( cdl, "planAndImplementationCaching", "Both" );

        progressReportBase = 100;
        numberOfThreads = Integer.parseInt( cdl.get( "numberOfThreads" ) );
        numberOfWarmUpIterations = Integer.parseInt( cdl.get( "numberOfWarmUpIterations" ) );

        if ( Boolean.parseBoolean( cdl.get( "useRandomSeeds" ) ) ) {
            Random tempRand = new Random();
            randomSeedInsert = tempRand.nextLong();
            randomSeedQuery = tempRand.nextLong();
        } else {
            randomSeedInsert = Long.parseLong( cdl.get( "randomSeedInsert" ) );
            randomSeedQuery = Long.parseLong( cdl.get( "randomSeedQuery" ) );
        }

        dimensionFeatureVectors = Integer.parseInt( cdl.get( "dimensionFeatureVectors" ) );
        batchSizeInserts = Integer.parseInt( cdl.get( "batchSizeInserts" ) );
        numberOfEntries = Integer.parseInt( cdl.get( "numberOfEntries" ) );

        batchSizeQueries = Integer.parseInt( cdl.get( "batchSizeQueries" ) );
        numberOfSimpleKnnIntFeatureQueries = Integer.parseInt( cdl.get( "numberOfSimpleKnnIntFeatureQueries" ) );
        numberOfSimpleKnnRealFeatureQueries = Integer.parseInt( cdl.get( "numberOfSimpleKnnRealFeatureQueries" ) );
        numberOfSimpleMetadataQueries = Integer.parseInt( cdl.get( "numberOfSimpleMetadataQueries" ) );
        numberOfSimpleKnnIdIntFeatureQueries = Integer.parseInt( cdl.get( "numberOfSimpleKnnIdIntFeatureQueries" ) );
        numberOfSimpleKnnIdRealFeatureQueries = Integer.parseInt( cdl.get( "numberOfSimpleKnnIdRealFeatureQueries" ) );
        numberOfMetadataKnnIntFeatureQueries = Integer.parseInt( cdl.get( "numberOfMetadataKnnIntFeatureQueries" ) );
        numberOfMetadataKnnRealFeatureQueries = Integer.parseInt( cdl.get( "numberOfMetadataKnnRealFeatureQueries" ) );
//        numberOfCombinedQueries = getIntProperty( properties, "numberOfCombinedQueries" ) * multiplier;
        limitKnnQueries = Integer.parseInt( cdl.get( "limitKnnQueries" ) );
        distanceNorm = cdl.get( "distanceNorm" ).trim();
    }


    @Override
    public boolean usePreparedBatchForDataInsertion() {
        return true;
    }

}
