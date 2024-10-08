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

import com.github.rvesse.airline.Cli;
import com.github.rvesse.airline.builder.CliBuilder;
import java.sql.SQLException;


@SuppressWarnings("unchecked")
public class Main {


    public static void main( String[] args ) throws SQLException {
        CliBuilder<CliRunnable> builder = Cli.builder( "polypheny-simple-client.jar" );
        builder.withDescription( "Polypheny Simple Client" );

        // define top level commands
        builder.withCommands( ChronosCommand.class );
        builder.withCommands( ComsCommand.class );
        builder.withCommands( GavelCommand.class );
        builder.withCommands( KnnCommand.class );
        builder.withCommands( MultimediaCommand.class );
        builder.withCommands( GraphCommand.class );
        builder.withCommands( LdbcSnbCommand.class );
        builder.withCommands( DocBenchCommand.class );
        builder.withCommands( MultiBenchCommand.class );
        builder.withCommands( AuctionMarkCommand.class );
        builder.withCommands( SmallBankCommand.class );
        builder.withCommands( TpccCommand.class );
        builder.withCommands( TpchCommand.class );
        builder.withCommands( YcsbCommand.class );
        builder.withCommands( DumpCommand.class );
        builder.withCommands( HelpCommand.class );
        builder.withDefaultCommand( HelpCommand.class );

        Cli<CliRunnable> cli = builder.build();
        HelpCommand.global = cli.getMetadata();

        CliRunnable cmd = cli.parse( args );
        cmd.run();
    }

}
