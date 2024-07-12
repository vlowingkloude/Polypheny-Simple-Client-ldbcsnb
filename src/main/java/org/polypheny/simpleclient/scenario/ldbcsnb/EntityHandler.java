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

import java.util.List;
import java.util.Map;

public abstract class EntityHandler {
    public abstract String getPath(String pathPrefix);
    public abstract String getQuery(List<String> row);

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
