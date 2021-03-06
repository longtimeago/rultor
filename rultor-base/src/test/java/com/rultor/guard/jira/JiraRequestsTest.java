/**
 * Copyright (c) 2009-2013, rultor.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rultor.guard.jira;

import com.rultor.ext.jira.Jira;
import com.rultor.ext.jira.JiraIssue;
import com.rultor.guard.MergeRequest;
import com.rultor.guard.MergeRequests;
import java.util.Arrays;
import java.util.Collections;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link JiraRequests}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class JiraRequestsTest {

    /**
     * JiraRequests can send issues through.
     * @throws Exception If some problem inside
     */
    @Test
    public void sendsIssuesThrough() throws Exception {
        final JiraIssue issue = Mockito.mock(JiraIssue.class);
        final Jira jira = Mockito.mock(Jira.class);
        Mockito.doReturn(Collections.singletonList(issue)).when(jira)
            .search(Mockito.anyString());
        final Refinement refinement = Refinement.NONE;
        final MergeRequests requests = new JiraRequests(jira, "", refinement);
        MatcherAssert.assertThat(
            requests,
            Matchers.<MergeRequest>hasItem(Matchers.notNullValue())
        );
    }

    /**
     * JiraRequests can refine multiple times.
     * @throws Exception If some problem inside
     */
    @Test
    public void refinesMultipleTimes() throws Exception {
        final JiraIssue issue = Mockito.mock(JiraIssue.class);
        final Jira jira = Mockito.mock(Jira.class);
        Mockito.doReturn(Collections.singletonList(issue)).when(jira)
            .search(Mockito.anyString());
        final Refinement first = Refinement.NONE;
        final Refinement second = Refinement.NONE;
        final MergeRequests requests = new JiraRequests(
            jira, "", Arrays.asList(first, second)
        );
        MatcherAssert.assertThat(
            requests,
            Matchers.<MergeRequest>hasItem(Matchers.notNullValue())
        );
    }

}
