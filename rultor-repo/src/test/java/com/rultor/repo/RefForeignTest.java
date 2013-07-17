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
package com.rultor.repo;

import com.jcabi.urn.URN;
import com.rultor.spi.Arguments;
import com.rultor.spi.Spec;
import com.rultor.spi.Unit;
import com.rultor.spi.User;
import com.rultor.spi.Users;
import com.rultor.spi.Variable;
import com.rultor.spi.Work;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link RefForeign}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class RefForeignTest {

    /**
     * RefForeign can make an instance.
     * @throws Exception If some problem inside
     */
    @Test
    public void makesInstance() throws Exception {
        final String name = "some-ref-name";
        final Unit unit = Mockito.mock(Unit.class);
        final Spec spec = new Spec.Simple("java.lang.Long(1L)");
        Mockito.doReturn(spec).when(unit).spec();
        final User user = Mockito.mock(User.class);
        Mockito.doReturn(new HashSet<String>(Arrays.asList(name)))
            .when(user).units();
        Mockito.doReturn(unit).when(user).get(name);
        final URN urn = new URN("urn:facebook:1");
        Mockito.doReturn(urn).when(user).urn();
        final Variable<Object> var = new RefForeign(
            new AntlrGrammar(), urn, urn, name,
            new ArrayList<Variable<?>>(0)
        );
        final Users users = Mockito.mock(Users.class);
        Mockito.doReturn(user).when(users).get(urn);
        MatcherAssert.assertThat(
            var.instantiate(users, new Arguments(new Work.None())),
            Matchers.<Object>equalTo(1L)
        );
    }

    /**
     * RefForeign can make a text.
     * @throws Exception If some problem inside
     */
    @Test
    public void makesText() throws Exception {
        final URN urn = new URN("urn:facebook:998");
        final Variable<Object> var = new RefForeign(
            new AntlrGrammar(), urn, urn, "some-name",
            new ArrayList<Variable<?>>(0)
        );
        MatcherAssert.assertThat(
            var.asText(),
            Matchers.equalTo("urn:facebook:998:some-name()")
        );
    }

}
