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
package com.rultor.scm.git;

import com.google.common.io.Files;
import com.rultor.scm.Branch;
import com.rultor.shell.ShellMocker;
import java.net.URI;
import java.util.Collection;
import javax.validation.ConstraintViolationException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Git}.
 * @author Bharath Bolisetty (bharathbolisetty@gmail.com)
 * @author Evgeniy Nyavro (e.nyavro@gmail.com)
 * @version $Id$
 */
public final class GitTest {

    /**
     * URL to public GitHub repository.
     */
    private static final URI REPO =
        URI.create("http://github.com/yegor256/xembly.git");

    /**
     * Git public ctor args can not be null.
     * @throws Exception If some problem inside
     */
    @Test(expected = ConstraintViolationException.class)
    public void failsWhenInitializedWithNulls() throws Exception {
        new Git(null, new URI("ssh://git@github.com:rultor/rultor.git"), null);
    }

    /**
     * Can checkout branch.
     * @throws Exception if some problem inside
     */
    @Test
    public void checksOutBranch() throws Exception {
        MatcherAssert.assertThat(
            new Git(
                new ShellMocker.Bash(Files.createTempDir()),
                GitTest.REPO,
                "test"
            ).checkout("master"),
            Matchers.notNullValue(Branch.class)
        );
    }

    /**
     * Can get branches.
     * @throws Exception if some problem inside
     */
    @Test
    public void getsBranches() throws Exception {
        MatcherAssert.assertThat(
            new Git(
                new ShellMocker.Bash(Files.createTempDir()),
                GitTest.REPO,
                "test2"
            ).branches(),
            Matchers.notNullValue(Collection.class)
        );
    }
}
