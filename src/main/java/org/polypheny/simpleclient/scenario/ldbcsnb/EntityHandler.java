/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-6/24/24, 2:08 PM The Polypheny Project
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

import org.polypheny.simpleclient.scenario.ldbcsnb.entities.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class EntityHandler {
    public abstract String getPath(String pathPrefix, String scaleFactor);
    public abstract String getQuery(List<String> row);


    protected static String toEpochMillis( String value ) {
        long epochMillis;
        try {
            epochMillis = Long.parseLong( value );
            return epochMillis + ".0";
        } catch ( NumberFormatException ignored ) {
            // Continue with the string-based formats used by LDBC datasets.
        }

        try {
            epochMillis = Instant.parse( value ).toEpochMilli();
            return epochMillis + ".0";
        } catch ( DateTimeParseException ignored ) {
            // Some datasets use an explicit offset rather than the UTC suffix.
        }

        try {
            epochMillis = OffsetDateTime.parse( value ).toInstant().toEpochMilli();
            return epochMillis + ".0";
        } catch ( DateTimeParseException ignored ) {
            // Birthdays contain a date without a time component.
        }

        epochMillis = LocalDate.parse( value ).atStartOfDay().toInstant( ZoneOffset.UTC ).toEpochMilli();
        return epochMillis + ".0";
    }


    protected static String canonicalLabel( String value ) {
        if ( value == null || value.isEmpty() ) {
            throw new IllegalArgumentException( "LDBC label must not be empty" );
        }
        return Character.toUpperCase( value.charAt( 0 ) ) + value.substring( 1 );
    }


    protected static String quote( String value ) {
        return '"' + value
                .replace( "\\", "\\\\" )
                .replace( "'", "\\\\'" )
                .replace( "\"", "\\\"" )
                .replace( ";", "\\u003B" )
                .replace( "\n", "\\n" )
                .replace( "\r", "\\r" ) + '"';
    }


    protected static String stringList( String value ) {
        if ( value == null || value.isBlank() ) {
            return "[]";
        }
        return Arrays.stream( value.split( ";", -1 ) )
                .map( EntityHandler::quote )
                .collect( Collectors.joining( ", ", "[", "]" ) );
    }

    public static EntityHandler[] getEntities() {
        return new EntityHandler[] {
                new Place(),
                new Organisation(),
                new TagClass(),
                new Tag(),
                new Forum(),
                new Person(),
                new Comment(),
                new Post(),
                new PlaceIsPartOfPlace(),
                new TagClassIsSubclassOfTagClass(),
                new OrganisationIsLocatedInPlace(),
                new TagHasTypeTagClass(),
                new CommentHasCreatorPerson(),
                new CommentIsLocatedInCountry(),
                new CommentReplyOfComment(),
                new CommentReplyOfPost(),
                new ForumContainerOfPost(),
                new ForumHasMemberPerson(),
                new ForumHasModeratorPerson(),
                new ForumHasTagTag(),
                new PersonHasInterestTag(),
                new PersonIsLocatedInCity(),
                new PersonKnowsPerson(),
                new PersonLikesComment(),
                new PersonLikesPost(),
                new PostHasCreatorPerson(),
                new CommentHasTagTag(),
                new PostHasTagTag(),
                new PostIsLocatedInCountry(),
                new PersonStudyAtUniversity(),
                new PersonWorkAtCompany(),
        };
    }
}
