/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-2026 The Polypheny Project
 */

package org.polypheny.simpleclient.scenario.ldbcsnb;

import java.util.List;
import org.polypheny.simpleclient.scenario.ldbcsnb.queries.InteractiveComplex1;
import org.polypheny.simpleclient.scenario.ldbcsnb.queries.InteractiveComplex10;
import org.polypheny.simpleclient.scenario.ldbcsnb.queries.InteractiveComplex11;
import org.polypheny.simpleclient.scenario.ldbcsnb.queries.InteractiveComplex12;
import org.polypheny.simpleclient.scenario.ldbcsnb.queries.InteractiveComplex13;
import org.polypheny.simpleclient.scenario.ldbcsnb.queries.InteractiveComplex14;
import org.polypheny.simpleclient.scenario.ldbcsnb.queries.InteractiveComplex2;
import org.polypheny.simpleclient.scenario.ldbcsnb.queries.InteractiveComplex3;
import org.polypheny.simpleclient.scenario.ldbcsnb.queries.InteractiveComplex4;
import org.polypheny.simpleclient.scenario.ldbcsnb.queries.InteractiveComplex5;
import org.polypheny.simpleclient.scenario.ldbcsnb.queries.InteractiveComplex6;
import org.polypheny.simpleclient.scenario.ldbcsnb.queries.InteractiveComplex7;
import org.polypheny.simpleclient.scenario.ldbcsnb.queries.InteractiveComplex8;
import org.polypheny.simpleclient.scenario.ldbcsnb.queries.InteractiveComplex9;


public final class LdbcSnbQueries {

    private static final List<Integer> COMPLEX_QUERY_NUMBERS = List.of( 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 );


    private LdbcSnbQueries() {
    }


    public static List<Integer> complexQueryNumbers() {
        return COMPLEX_QUERY_NUMBERS;
    }


    public static LdbcSnbQuery complex( int queryNumber ) {
        return switch ( queryNumber ) {
            case 1 -> new InteractiveComplex1();
            case 2 -> new InteractiveComplex2();
            case 3 -> new InteractiveComplex3();
            case 4 -> new InteractiveComplex4();
            case 5 -> new InteractiveComplex5();
            case 6 -> new InteractiveComplex6();
            case 7 -> new InteractiveComplex7();
            case 8 -> new InteractiveComplex8();
            case 9 -> new InteractiveComplex9();
            case 10 -> new InteractiveComplex10();
            case 11 -> new InteractiveComplex11();
            case 12 -> new InteractiveComplex12();
            case 13 -> new InteractiveComplex13();
            case 14 -> new InteractiveComplex14();
            default -> throw new IllegalArgumentException( "LDBC complex query number must be in [1, 14]" );
        };
    }

}
